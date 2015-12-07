package com.inqool.dcap.integration.z3950.sru.server.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.z3950.sru.server.CouldNotParseCqlException;
import com.inqool.dcap.integration.z3950.sru.server.CqlToSolr;
import com.inqool.dcap.integration.z3950.sru.server.config.SruDiagnosticsConstants;
import com.inqool.dcap.integration.z3950.sru.server.request.SearchRetrieveRequest;
import info.srw.schema._1.dc_schema.ObjectFactory;
import info.srw.schema._1.dc_schema.SrwDcType;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jboss.resteasy.annotations.Form;
import org.oasis_open.docs.ns.search_ws.diagnostic.DiagnosticComplexType;
import org.oasis_open.docs.ns.search_ws.sruresponse.*;
import org.purl.dc.elements._1.ElementType;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
@RequestScoped
public class SearchRetrieveResource extends OperationResource {

    @Inject
    @ConfigProperty(name = "solr.endpoint.main")
    private String SOLR_MAIN_ENDPOINT;

    @Inject
    private CqlToSolr cqlToSolrConverter;

    @Inject
    @Zdo
    private Logger logger;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public SearchRetrieveResponseDefinition handle(@Form SearchRetrieveRequest searchRetrieveRequest) {
        SearchRetrieveResponseDefinition searchRetrieveResponse = new SearchRetrieveResponseDefinition();
        //Version
        String version = searchRetrieveRequest.getVersion();
        if("1.1".equals(version)) searchRetrieveResponse.setVersion("1.1");
        else if("1.2".equals(version)) searchRetrieveResponse.setVersion("1.2");
        else searchRetrieveResponse.setVersion("2.0");

        String recSchema = searchRetrieveRequest.getRecordSchema();
        if(recSchema == null) recSchema = "dc";
        //Allow only DC schema
        if(!("dc".equals(recSchema) || "info:srw/schema/1/dc-v1.1".equals(recSchema))) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNKNOWN_SCHEMA_FOR_RETRIEVAL, recSchema, "Schema " + recSchema + " is not supported, please use \ndc\n.");
            return searchRetrieveResponse;
        }
        //Not allow stylesheets
        if(searchRetrieveRequest.getStylesheet() != null) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.STYLESHEETS_NOT_SUPPORTED, null, "Stylesheets not supported.");
        }
        //Not allow TTL
        if(searchRetrieveRequest.getResultSetTTL() != null) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER, "resultSetTTL", "ResultSetTTL not supported.");
        }
        //Not allow sortKeys
        if(searchRetrieveRequest.getSortKeys() != null) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.SORT_NOT_SUPPORTED, "sortKeys", "SortKeys not supported.");
        }
        //Not allow facets
        if(searchRetrieveRequest.getFacetCount() != null ||
                searchRetrieveRequest.getFacetLimit() != null ||
                searchRetrieveRequest.getFacetSort() != null ||
                searchRetrieveRequest.getFacetStart() != null) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER, "facet", "Facets not supported.");
        }
        //Allow only xml packing
        if(searchRetrieveRequest.getRecordPacking() != null && !"xml".equals(searchRetrieveRequest.getRecordPacking())) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER_VALUE, "recordPacking", "Only xml record packing supported.");
            return searchRetrieveResponse;
        }
        //Parse start record arg
        String startRecordString = searchRetrieveRequest.getStartRecord();
        int startRecord;
        if(startRecordString == null) {
            startRecord = 1;
        }
        else {
            try {
                startRecord = Integer.valueOf(startRecordString);
                if(startRecord<1) throw new NumberFormatException("Start record must be greater than 0.");
            } catch (NumberFormatException e) {
                addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER_VALUE, "startRecord", "Invalid value for startRecord, please supply a positive integer.");
                return searchRetrieveResponse;
            }
        }
        //Parse max records arg
        String maxRecordsString = searchRetrieveRequest.getMaximumRecords();
        int maxRecords;
        if(maxRecordsString == null) {
            maxRecords = 10;
        }
        else {
            try {
                maxRecords = Integer.valueOf(maxRecordsString);
            } catch (NumberFormatException e) {
                addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER_VALUE, "maximumRecords", "Invalid value for maximumRecords, please supply an integer.");
                return searchRetrieveResponse;
            }
        }
        //Check query arg
        String cqlQuery = searchRetrieveRequest.getQuery();
        if(cqlQuery == null) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.MANDATORY_PARAMETER_NOT_SUPPLIED, "query", "Mandatory parameter query not supplied.");
            return searchRetrieveResponse;
        }
        if("".equals(cqlQuery)) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER_VALUE, "query", "Invalid value for parameter query.");
            return searchRetrieveResponse;
        }
        try {
            //Convert query from CQL to Lucene/Solr, check errors
            String convertedQuery = null;
            List<DiagnosticComplexType> diagnostics = new ArrayList<>();
            try {
                convertedQuery = cqlToSolrConverter.convert(cqlQuery, diagnostics);
            } catch(CouldNotParseCqlException e) {
                logger.debug("A query could not be parsed.", e);
            }
            if(diagnostics.size() > 0) {
                addDiagnostics(searchRetrieveResponse, diagnostics);
                return searchRetrieveResponse;
            }

            //Query the Solr
            SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(convertedQuery);
            solrQuery.setStart(startRecord-1);
            solrQuery.setRows(maxRecords);
            QueryResponse rsp = server.query(solrQuery);

            //Go through solr results and put them into response
            RecordsDefinition recordsDefinition = new RecordsDefinition();
            List<RecordDefinition> records = recordsDefinition.getRecord();
            SolrDocumentList docs = rsp.getResults();
            int recordPosition = startRecord;
            for(SolrDocument doc : docs) {
                //Rebuild dublin core from results
                SrwDcType dc = new SrwDcType();
                for(String property : doc.keySet()) {
                    Object value = doc.get(property);
                    if(value instanceof String) {
                        addDcElement(property, (String) value, dc);
                    }
                    else if(value instanceof List) {
                        for(Object innerValue : ((List) value)) {
                            if(innerValue instanceof String) {
                                addDcElement(property, (String) innerValue, dc);
                            }
                        }
                    }
                }
                //Create result record and put DC into it
                RecordDefinition record = new RecordDefinition();
                record.setRecordSchema("info:srw/schema/1/dc-v1.1");
                record.setRecordXMLEscaping(RecordXMLEscapingDefinition.fromValue("xml"));
                record.setRecordPacking("xml");
                StringOrXmlFragmentDefinition stringOrXmlFragmentDefinition = new StringOrXmlFragmentDefinition();
                ObjectFactory of = new ObjectFactory();
                stringOrXmlFragmentDefinition.getContent().add(of.createDc(dc));
                record.setRecordData(stringOrXmlFragmentDefinition);
                record.setRecordPosition(BigInteger.valueOf(recordPosition++));

                records.add(record);
            }
            //Set record position field
            if(recordPosition<=docs.getNumFound()) {
                searchRetrieveResponse.setNextRecordPosition(BigInteger.valueOf(recordPosition));
            }
            //Set total number of records field
            searchRetrieveResponse.setNumberOfRecords(BigInteger.valueOf(docs.getNumFound()));
            //Check if requested record number is not out of range
            if(startRecord>docs.getNumFound() && docs.getNumFound() > 0) {
                addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.FIRST_RECORD_POSITION_OUT_OF_RANGE, null, "First record position out of range.");
            }
            searchRetrieveResponse.setRecords(recordsDefinition);
        } catch (SolrServerException e) {
            addDiagnostics(searchRetrieveResponse, SruDiagnosticsConstants.GENERAL_SYSTEM_ERROR, null, "Server error.");
            e.printStackTrace();
        }

        return searchRetrieveResponse;
    }

    /**
     * Adds value to given property of dublin core record
     * @param dcProp property to be set
     * @param dcVal the value to be given to property
     * @param dc dublin core to which value will be added
     */
    private void addDcElement(String dcProp, String dcVal, SrwDcType dc) {
        org.purl.dc.elements._1.ObjectFactory of = new org.purl.dc.elements._1.ObjectFactory();
        JAXBElement<ElementType> dcElement;
        ElementType elementType = new ElementType();
        elementType.setValue(dcVal);
        switch (dcProp) {
            case "title":
                dcElement = of.createTitle(elementType);
                break;
            case "identifier":
                dcElement = of.createIdentifier(elementType);
                break;
            case "creator":
                dcElement = of.createCreator(elementType);
                break;
            case "subject":
                dcElement = of.createSubject(elementType);
                break;
            case "description":
                dcElement = of.createDescription(elementType);
                break;
            case "publisher":
                dcElement = of.createPublisher(elementType);
                break;
            case "contributor":
                dcElement = of.createContributor(elementType);
                break;
            case "date":
                dcElement = of.createDate(elementType);
                break;
            case "type":
                dcElement = of.createType(elementType);
                break;
            case "format":
                dcElement = of.createFormat(elementType);
                break;
            case "source":
                dcElement = of.createSource(elementType);
                break;
            case "language":
                dcElement = of.createLanguage(elementType);
                break;
            case "relation":
                dcElement = of.createRelation(elementType);
                break;
            case "coverage":
                dcElement = of.createCoverage(elementType);
                break;
            case "rights":
                dcElement = of.createRights(elementType);
                break;
            default:
                return;
        }
        dc.getTitleOrCreatorOrSubject().add(dcElement);
    }

    /**
     * Adds single diagnostic into response
     * @param searchRetrieveResponse
     * @param number
     * @param details
     * @param message
     */
    private void addDiagnostics(SearchRetrieveResponseDefinition searchRetrieveResponse, int number, String details, String message) {
        DiagnosticsDefinition diagnostics = searchRetrieveResponse.getDiagnostics();
        if(diagnostics == null) diagnostics = new DiagnosticsDefinition();
        {
            DiagnosticComplexType diagnostic = new DiagnosticComplexType();
            diagnostic.setUri(SruDiagnosticsConstants.DIAGNOSTIC_URI_PREFIX + number);
            diagnostic.setDetails(details);
            diagnostic.setMessage(message);
            diagnostics.getDiagnostic().add(diagnostic);
        }
        searchRetrieveResponse.setDiagnostics(diagnostics);
    }

    /**
     * Adds all diagnostics from list to response
     * @param searchRetrieveResponse
     * @param toAdd
     */
    private void addDiagnostics(SearchRetrieveResponseDefinition searchRetrieveResponse, List<DiagnosticComplexType> toAdd) {
        DiagnosticsDefinition diagnostics = searchRetrieveResponse.getDiagnostics();
        if(diagnostics == null) diagnostics = new DiagnosticsDefinition();
        diagnostics.getDiagnostic().addAll(toAdd);
        searchRetrieveResponse.setDiagnostics(diagnostics);
    }
}
