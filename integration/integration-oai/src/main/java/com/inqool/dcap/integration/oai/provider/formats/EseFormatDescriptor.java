package com.inqool.dcap.integration.oai.provider.formats;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.oai.provider.datasource.OaiDataSource;
import com.inqool.dcap.integration.oai.provider.exception.CannotDisseminateFormatPmhException;
import com.inqool.dcap.integration.oai.provider.exception.IdDoesNotExistPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoRecordsMatchPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoSetHierarchyPmhException;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoOrganization;
import eu.europeana.schemas.ese.Record;
import org.openarchives.oai._2.*;
import org.purl.dc.elements._1.SimpleLiteral;
import org.purl.dc.terms.ObjectFactory;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Lukas Jane (inQool) 1. 5. 2015.
 */
@RequestScoped
public class EseFormatDescriptor implements FormatDescriptor {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OaiDataSource oaiDataSource;

    @Inject
    private DataStore store;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    private Map<String, String> orgNameMapping = new HashMap<>();

    public static final String DATA_PROVIDER_DEFAULT = "Zlínský kraj, Česká Republika";
    public static final String PROVIDER = "Národní muzeum, National Museum, Czech Republic";
    public static final String RIGHTS = "http://creativecommons.org/licenses/by/4.0/";
    public static final String DOC_URL_BASE = "http://ebadatelna-test.zlkraj.cz/";

    private enum Type {
        TEXT("TEXT"),
        IMAGE("IMAGE"),
        SOUND("SOUND"),
        VIDEO("VIDEO"),
        x3D("3D");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public String getMetadataPrefix() {
        return "ese";
    }
    @Override
    public String getMetadataNamespace() {
        return "http://www.europeana.eu/schemas/ese/";
    }
    @Override
    public String getSchema() {
        return "http://www.europeana.eu/schemas/ese/ESE-V3.4.xsd";
    }

    @Override
    public GetRecordType getRecord(String identifier) throws CannotDisseminateFormatPmhException, IdDoesNotExistPmhException {
        GetRecordType result = new GetRecordType();
        Map<String, List<String>> recordMap = oaiDataSource.getRecordDataToMap(identifier);
        RecordType record = mapToRecordType(recordMap);
        result.setRecord(record);
        result.getRecord().getHeader().setIdentifier(identifier);   //add record identifier
        return result;
    }

    @Override
    public ListRecordsType listRecords(OffsetDateTime from, OffsetDateTime until, String set, int page) throws NoRecordsMatchPmhException, NoSetHierarchyPmhException {
        ListRecordsType listRecordsType = new ListRecordsType();
        List<RecordType> records = listRecordsType.getRecords();

        AtomicBoolean hasMore = new AtomicBoolean(false);
        Map<String, Map<String, List<String>>> recordsMap = oaiDataSource.listRecordDataToMap(from, until, set, page, hasMore);

        //Create an OAI-PMH record from each subject in map
        for(String subject : recordsMap.keySet()) {
            RecordType record = mapToRecordType(recordsMap.get(subject));
            record.getHeader().setIdentifier(subject); //add record identifier
            records.add(record);
        }
        //There are more results
        if(hasMore.get()) {
            //Reconstruct resumption token
            String resumptionToken =
                    from.format(DateTimeFormatter.ISO_LOCAL_DATE) + "." +
                            until.format(DateTimeFormatter.ISO_LOCAL_DATE) + "." +
                            getMetadataPrefix() + "." +
                            (set == null ? "" : set) + "." +
                            (page + 1);
            ResumptionTokenType resumptionTokenType = new ResumptionTokenType();
            resumptionTokenType.setValue(resumptionToken);
            listRecordsType.setResumptionToken(resumptionTokenType);
        }
        return listRecordsType;
    }

    @Override
    public ListIdentifiersType listIdentifiers(OffsetDateTime from, OffsetDateTime until, String set, int page) throws NoRecordsMatchPmhException, CannotDisseminateFormatPmhException, NoSetHierarchyPmhException {
        ListIdentifiersType listIdentifiersType = new ListIdentifiersType();
        List<HeaderType> headers = listIdentifiersType.getHeaders();

        AtomicBoolean hasMore = new AtomicBoolean(false);
        Map<String, String> identifiersMap = oaiDataSource.listIdentifiersToMap(from, until, set, page, hasMore);
        for(String identifier : identifiersMap.keySet()) {
            HeaderType header = new HeaderType();
            header.setIdentifier(identifier);
            header.setDatestamp(identifiersMap.get(identifier));
            headers.add(header);
        }
        //There are more results
        if(hasMore.get()) {
            //Reconstruct resumption token
            String resumptionToken =
                    from.format(DateTimeFormatter.ISO_LOCAL_DATE) + "." +
                            until.format(DateTimeFormatter.ISO_LOCAL_DATE) + "." +
                            getMetadataPrefix() + "." +
                            (set == null ? "" : set) + "." +
                            (page + 1);
            ResumptionTokenType resumptionTokenType = new ResumptionTokenType();
            resumptionTokenType.setValue(resumptionToken);
            listIdentifiersType.setResumptionToken(resumptionTokenType);
        }
        return listIdentifiersType;
    }

    /**
     * Converts given map of lists of property values to a serializable ESE object
     * @param map map where keys are property names and values are lists of property values
     * @return recordType
     */
    private RecordType mapToRecordType(Map<String, List<String>> map) {
        RecordType recordType = new RecordType();

        //Header with lastModified time
        List<String> lastModifiedList = map.get("lastModified");
        if(lastModifiedList == null || lastModifiedList.size() != 1) {
            throw new RuntimeException("Didn't find lastModified field in record or there were multiple.");
        }
        HeaderType header = new HeaderType();
        header.setDatestamp(lastModifiedList.get(0));
        recordType.setHeader(header);

        //Record metadata
        MetadataType metadataType = new MetadataType();
        Record record = new Record();
        for(String dcProp : map.keySet()) {
            for(String dcVal : map.get(dcProp)) {
                addDcElement(dcProp, dcVal, record);
            }
        }

        //Order is important
        /*record.getUnstoreds().add()*/
        String imgUuid = null;
        if(map.get("imgThumb") != null) {
            imgUuid = map.get("imgNormal").get(0);
            String thumbUrl = DOC_URL_BASE + "dcap/discovery/data/file/" + imgUuid;
            record.setObject(thumbUrl);
        }

        record.setProvider(PROVIDER);

        String zdoType = map.get("zdoType").get(0);
        switch (zdoType) {
            case ZdoType.CHO:
                record.setType(Type.IMAGE.getName());
                break;
            case ZdoType.MONOGRAPH:
            case ZdoType.PERIODICAL:
            case ZdoType.BORNDIGITAL:
            default:
                record.setType(Type.TEXT.getName());
        }

        record.setRights(RIGHTS);

        if(orgNameMapping.isEmpty()) {
            fetchOrgNames();
        }
        String dataProvider = DATA_PROVIDER_DEFAULT;
        if(map.get("organization") != null) {
            String orgName = orgNameMapping.get(map.get("organization").get(0));
            if(orgName != null) {
                dataProvider = orgName;
            }
        }
        record.setDataProvider(dataProvider);
        eu.europeana.schemas.ese.ObjectFactory of = new eu.europeana.schemas.ese.ObjectFactory();
        record.getIsShownBiesAndIsShownAts().add(of.createIsShownAt(DOC_URL_BASE + "search?query=inventoryId:\\\"" + map.get("inventoryId").get(0) + "\\\""));
        /*record.setUGC();*/

        metadataType.setAny(record);
        recordType.setMetadata(metadataType);

        return recordType;
    }

    /**
     * Adds DC element to given DC object
     * @param dcProp property
     * @param dcVal value
     * @param record ese record object to add element to
     */
    private void addDcElement(String dcProp, String dcVal, Record record) {
        org.purl.dc.elements._1.ObjectFactory of = new org.purl.dc.elements._1.ObjectFactory();
        JAXBElement<SimpleLiteral> dcElement;
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(dcVal);
        switch (dcProp) {
            case "title":
                dcElement = of.createTitle(simpleLiteral);
                break;
            case "identifier":
                dcElement = of.createIdentifier(simpleLiteral);
                break;
            case "creator":
                dcElement = of.createCreator(simpleLiteral);
                break;
            case "subject":
                dcElement = of.createSubject(simpleLiteral);
                break;
            case "description":
                dcElement = of.createDescription(simpleLiteral);
                break;
            case "publisher":
                dcElement = of.createPublisher(simpleLiteral);
                break;
            case "contributor":
                dcElement = of.createContributor(simpleLiteral);
                break;
            case "date":
                dcElement = of.createDate(simpleLiteral);
                break;
            case "type":
                dcElement = of.createType(simpleLiteral);
                break;
            case "format":
                dcElement = of.createFormat(simpleLiteral);
                break;
            case "source":
                dcElement = of.createSource(simpleLiteral);
                break;
            case "language":
                dcElement = of.createLanguage(simpleLiteral);
                break;
            case "relation":
                dcElement = of.createRelation(simpleLiteral);
                break;
            case "coverage":
                dcElement = of.createCoverage(simpleLiteral);
                break;
            case "rights":
                dcElement = of.createRights(simpleLiteral);
                break;
            case "provenance":
                org.purl.dc.terms.ObjectFactory ofTerms = new ObjectFactory();
                dcElement = ofTerms.createProvenance(simpleLiteral);
                break;
            default:    //do not do anything if property does not belong to Dublin Core
                return;
        }
        record.getTitlesAndCreatorsAndSubjects().add(dcElement);
    }

    private void fetchOrgNames() {
        List<ZdoOrganization> orgList = picketLinkAccess.listOrganizations();
        for(ZdoOrganization org : orgList) {
            String idmId = org.getName();
            String displayName = org.getDisplayName();
            displayName = displayName.replace(", příspěvková organizace", "").trim();
            orgNameMapping.put(idmId, displayName);
        }
    }
}
