package com.inqool.dcap.integration.oai.harvester;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.DCTools;
import com.inqool.dcap.MarcToDcConverter;
import com.inqool.dcap.common.entity.OaiSource;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.rdksys.oai.data.RecordIterator;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.RecordType;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Lukas Jane (inQool)
 */
@RequestScoped
public class OaiHarvester {

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private SolrExternalSourcesIndexer solrExternalSourcesIndexer;

    public boolean isWorkingOaiSource(String source) {
        try {
            Harvester harvester = new Harvester(source);
            IdentifyType identify = harvester.identify();
            return true;
        } catch (Exception e) {
            logger.error("Exception when checking OAI source.", e);
            e.printStackTrace();
            return false;
        }
    }

    public void harvestSource(OaiSource oaiSource) {
        try {
            Harvester harvester = new Harvester(oaiSource.getUrl());

            IdentifyType identify = harvester.identify();
            logger.info("Harvesting from " + identify.getRepositoryName() + ", " + identify.getProtocolVersion());

            String fromIsoTime;
            OffsetDateTime from;
            //Either use date of first repository record
            if(oaiSource.getLastHarvested() == null) {
                fromIsoTime = identify.getEarliestDatestamp();
                if(fromIsoTime.length() > 12) {
                    from = OffsetDateTime.parse(fromIsoTime);
                }
                else {
                    from = LocalDate.parse(fromIsoTime, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
                }
            }
            else {
                //Or convert time of last harvest to be able to continue from it onwards
                from = oaiSource.getLastHarvested()
                        .atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
                        .atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
            }
/*                List<Set> sets = harvester.listSets();
                for(Set set : sets) {
                    System.out.println(set.getSetName());
                }*/

            //List metadata formats supported by the source and determine which one to use
            String formatToHarvest;
            List<MetadataFormatType> metadataFormatTypes = harvester.listMetadataFormats();
            Map<String, MetadataFormatType> metadataFormatTypeMap = new HashMap<>();
            for (MetadataFormatType metadataFormatType : metadataFormatTypes) {
                metadataFormatTypeMap.put(metadataFormatType.getMetadataPrefix(), metadataFormatType);
            }

            if(metadataFormatTypeMap.containsKey("marc21")) {
                formatToHarvest = "marc21";
            }
            else {
                formatToHarvest = metadataFormatTypes.get(0).getMetadataPrefix();
            }

            //Harvest one month worth of data at a time
            boolean didHaveMoreRecords;
            do {
                didHaveMoreRecords = false;
                if(from.isAfter(OffsetDateTime.now())) {
                    break;
                }
                fromIsoTime = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
                OffsetDateTime until = from.plusMonths(1);
                String untilIsoTime = until.format(DateTimeFormatter.ISO_LOCAL_DATE);
                // This can take a few minutes.
                logger.info("Calling list records, that might take a long while.");
                logger.debug("from " + fromIsoTime + " until " + untilIsoTime + " set " + oaiSource.getSet() + " format " + formatToHarvest);
                RecordIterator it =  harvester.listRecords(fromIsoTime, untilIsoTime, oaiSource.getSet(), formatToHarvest);
                try {
                    logger.info("Records listed.");
                    store.startTransaction();
                    int counter = 1;
                    try {
                        while (it.hasNext()) {
                            didHaveMoreRecords = true;
                            RecordType oaiRecord = it.next();
                            String oaiIdentifier = oaiRecord.getHeader().getIdentifier();
                            logger.debug("Parsing record " + counter++ + " " + oaiIdentifier);

                            //Check if we already have this record stored
                            String queryString = "SELECT ?s WHERE {\n" +
                                    " ?s <" + ZdoTerms.oaiIdentifier.getURI() + "> \"" + oaiIdentifier + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                                    " }";
                            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
                            ResultSet resultSet = queryExecution.execSelect();
                            ZdoModel zdoModel;
                            if (resultSet.hasNext()) {  //If we have it already
                                String existingModelId = resultSet.next().get("s").asResource().getURI();
                                zdoModel = store.get(existingModelId);

                                //Remove all old DC data
                                for (String propName : DCTools.getDcTermList()) {
                                    Property property = new PropertyImpl(DCTerms.NS + propName);
                                    zdoModel.removeAllValuesOfProperty(property);
                                }
                            } else {  //If not, create a new model
                                zdoModel = new ZdoModel(store.createUrl(UUID.randomUUID().toString()), ZdoType.monograph);
                                zdoModel.setIndexable(true);
                                zdoModel.add(ZdoTerms.group, ZdoGroup.EXTERNAL.name());
                                zdoModel.add(ZdoTerms.source, oaiSource.getUrl());
                                zdoModel.add(ZdoTerms.oaiIdentifier, oaiIdentifier);
                            }

                            //Unpack metadata
                            Object metadata = oaiRecord.getMetadata().getAny();
                            if (metadata instanceof JAXBElement) {
                                metadata = ((JAXBElement) metadata).getValue();
                            }

                            //Put metadata to model and save it to fedora
                            if (metadata instanceof gov.loc.marc21.slim.RecordType) {
                                gov.loc.marc21.slim.RecordType marcRecord = (gov.loc.marc21.slim.RecordType) metadata;
                                MarcToDcConverter marcToDcConverter = new MarcToDcConverter();
                                marcToDcConverter.convert(marcRecord, oaiSource.getShortcut(), zdoModel);
                            }
                            store.update(zdoModel);

                            //Index it to solr
                            solrExternalSourcesIndexer.update(zdoModel);
                        }
                    } finally {
                        store.commitTransaction();
                        solrExternalSourcesIndexer.commit();
                    }
                }
                finally {
                    it.close(); //We can't delete the file in next pass if its stream remains open
                }
                from = from.plusMonths(1);
            } while(didHaveMoreRecords);
            logger.info("OAI source " + oaiSource.getName() + " harvesting done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
