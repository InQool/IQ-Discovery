package com.inqool.dcap.office.api.core;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.DCTools;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.config.CustomProjectStageHolder;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.TriplestoreStuff;
import com.inqool.dcap.office.api.dto.DocDetailContainer;
import com.inqool.dcap.office.api.dto.ZdoDocumentBrief;
import com.inqool.dcap.office.api.dto.ZdoDocumentForExpo;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.office.api.request.DocumentListRequest;
import com.inqool.dcap.office.api.resource.Document;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoUser;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.picketlink.Identity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 25. 3. 2015.
 */
@RequestScoped
public class DocumentAccess {

    @Inject
    private SparqlTools sparqlTools;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Inject
    private DataStore store;

    @Inject
    private EntityManager em;

    @Inject
    private Identity identity;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    @Inject
    private NkListsAccess nkListsAccess;

    @Inject
    private ProjectStage projectStage;

    //When setting document type to issue, it needs to be set to periodical instead
    //When changing type for multiple issues of the same periodical, this holds traversed nodes, so we know we already changed it and can skip
    private Set<String> skipSet = new HashSet<>();

    @Transactional
    public List<ZdoDocumentBrief> listDocuments(DocumentListRequest request) {
        //Go through all triples and reconstruct property map, then turn it into nice object
        return triplestoreStuff.fromPropertyMap(sparqlTools.queryExecutionToPropertyMap(queryDocuments(request)));
    }

    @Transactional
    public DocDetailContainer getDocumentDetail(String docId) {
        ZdoModel model = store.get(store.createUrl(docId));
        if(model == null) throw new RuntimeException("No such document found.");

        if(!picketLinkAccess.getUsersOrganization().getName().equals(model.get(ZdoTerms.organization))) {
            throw new RuntimeException("This document belongs to different organization.");
        }

        String userIdmId = ((ZdoUser) identity.getAccount()).getLoginName();
        String docOwner = model.get(ZdoTerms.owner);
        if(docOwner != null && !userIdmId.equals(docOwner)) {
            throw new RuntimeException("This document belongs to someone else");
        }

        DocumentListRequest request = new DocumentListRequest();
        request.setInventoryId(model.get(ZdoTerms.inventoryId));
        request.setState(Document.zdoGroupToDocumentState(model.get(ZdoTerms.group)).name());

        //Go through all triples and reconstruct property map, then turn it into nice object
        Map<String, Map<String, List<String>>> propertyMap = sparqlTools.queryExecutionToPropertyMap(queryDocumentDetail(request));
        DocDetailContainer docDetailContainer = new DocDetailContainer();
        for (Map.Entry<String, Map<String, List<String>>> entry : propertyMap.entrySet()) {
            String docState = entry.getValue().get(ZdoTerms.group.getURI()).get(0);

            Map<String, List<String>> innerMap = filterForDocumentDetail(entry.getValue());
            innerMap.put("docId", Arrays.asList(store.getOnlyIdFromUrl(entry.getKey())));

            if (ZdoGroup.ZDO_CONCEPT.name().equals(docState) || ZdoGroup.ZDO.name().equals(docState)) {
                if (docDetailContainer.getConcept() != null)
                    throw new RuntimeException("Found more than one concept docs with this inventory id.");

                //Fill in batch name
                if (innerMap.get("batchId") != null && innerMap.get("batchId").size() == 1) {
                    ZdoBatch batch = em.find(ZdoBatch.class, Integer.valueOf(innerMap.get("batchId").get(0)));
                    if (batch != null) {
                        innerMap.put("batchName", Arrays.asList(batch.getName()));
                    }
                }

                docDetailContainer.setConcept(innerMap);
            } else if (ZdoGroup.KDR.name().equals(docState)) {
                if (docDetailContainer.getKdr() != null)
                    throw new RuntimeException("Found more than one KDR docs with this inventory id.");
                docDetailContainer.setKdr(innerMap);
            } else if (ZdoGroup.BACH.name().equals(docState)) {
                docDetailContainer.setBach(innerMap);
            } else if (ZdoGroup.DEMUS.name().equals(docState)) {
                docDetailContainer.setDemus(innerMap);
            } else if (ZdoGroup.EXTERNAL.name().equals(docState)) {
                String invId = innerMap.get("inventoryId").get(0);
                docDetailContainer.getOai().put(invId.substring(0, invId.indexOf("_")), innerMap);
            }
        }

        //If request was for unpublished or deleted doc, get exactly that and overwrite
        //This could be much cleaner and made together with above without duplicate code
        String group = model.get(ZdoTerms.group);
        if(ZdoGroup.UNPUBLISHED.name().equals(group) || ZdoGroup.DISCARDED.name().equals(group)) {
            String queryString = "SELECT ?subject ?property ?value WHERE {" +
                    "values ?subject { <" + store.removeTransactionFromUrl(model.getUrl()) + "> }" +
                    "  <" + store.removeTransactionFromUrl(model.getUrl()) + "> ?property ?value." +
                    " }";
            Map<String, Map<String, List<String>>> propertyMap2 = sparqlTools.queryExecutionToPropertyMap(QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString));
            for (Map.Entry<String, Map<String, List<String>>> entry : propertyMap2.entrySet()) {
                Map<String, List<String>> innerMap = filterForDocumentDetail(entry.getValue());
                innerMap.put("docId", Arrays.asList(store.getOnlyIdFromUrl(entry.getKey())));
                //Fill in batch name
                if (innerMap.get("batchId") != null && innerMap.get("batchId").size() == 1) {
                    ZdoBatch batch = em.find(ZdoBatch.class, Integer.valueOf(innerMap.get("batchId").get(0)));
                    if (batch != null) {
                        innerMap.put("batchName", Arrays.asList(batch.getName()));
                    }
                }
                docDetailContainer.setConcept(innerMap);
            }
        }

        return docDetailContainer;

        /*StmtIterator iter = model.listStatements();
        while(iter.hasNext()) {
            Statement statement = iter.next();
            String propNamepsace = statement.getPredicate().getNameSpace();
            if(!(propNamepsace.startsWith("http://inqool.cz/zdo") || propNamepsace.startsWith("http://purl.org/dc/terms"))) {
                continue;
            }

            String property = statement.getPredicate().getLocalName();
            RDFNode valNode = statement.getObject();
            String value;
            if(valNode.isLiteral()) {
                value = valNode.asLiteral().getString();
            }
            else {
                value = valNode.asResource().getURI();
            }

            if(!resultMap.containsKey(property)) {
                resultMap.put(property, new ArrayList<>());
            }
            resultMap.get(property).add(value);
        }

        resultMap.put("docId", Collections.singletonList(store.removeTransactionFromUrl(model.getUrl())));


        return resultMap;*/
    }

    public List<ZdoDocumentForExpo> listDocumentsForExpo(DocumentListRequest request) {
        //Go through all triples and reconstruct property map, then turn it into nice object
        Map<String, Map<String, List<String>>> subjectMap = sparqlTools.queryExecutionToPropertyMap(queryDocumentsForExpo(request));
        List<ZdoDocumentForExpo> result = new ArrayList<>();
        for(String modelUrl : subjectMap.keySet()) {
            ZdoDocumentForExpo zdoDocumentForExpo = new ZdoDocumentForExpo();
            Map<String, List<String>> propertyMap = subjectMap.get(modelUrl);
            zdoDocumentForExpo.setInvId(propertyMap.get(ZdoTerms.inventoryId.getURI()).get(0));
            List<String> titles = propertyMap.get(DCTerms.title.getURI());
            if(titles == null || titles.isEmpty()) {
                zdoDocumentForExpo.setTitle("Warning: Title missing.");
            }
            else {
                zdoDocumentForExpo.setTitle(propertyMap.get(DCTerms.title.getURI()).get(0));
            }
            zdoDocumentForExpo.setType(ZdoType.valueOf(propertyMap.get(ZdoTerms.zdoType.getURI()).get(0)));

            String orgIdmId = propertyMap.get(ZdoTerms.organization.getURI()).get(0);
            zdoDocumentForExpo.setOrgName(picketLinkAccess.getOrganizationName(orgIdmId));

            result.add(zdoDocumentForExpo);
        }
        return result;
    }

    private QueryExecution queryDocumentsForExpo(DocumentListRequest request) {
        //Limit number of results
        Integer limit = request.getLimit();
        String limitPart1 = "";
        String limitPart2 = "";
        if (limit != null && limit > 0) {
            limitPart1 = "  {\n" +
                    "    SELECT ?subject WHERE {\n";
            limitPart2 = "    } LIMIT " + limit + "\n" +
                    "  }\n";
        }

        String queryString = "SELECT ?subject ?property ?value WHERE {\n" +
                "  ?subject ?property ?value.\n" +
                limitPart1 +
                sparqlTools.createInventoryIdProxCondition(request.getInventoryId()) +
                sparqlTools.createDocumentStateCondition("published") +   //Filter documents by state
                sparqlTools.createRootTypeCondition() +
                limitPart2 +
                " }";
        return QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
    }

    //only let through DC and ZDO fields, and ditch their namespace prefixes
    private Map<String, List<String>> filterForDocumentDetail(Map<String, List<String>> inputMap) {
        Map<String, List<String>> outputMap = new HashMap<>();
        for (String key : inputMap.keySet()) {
            if (key.startsWith("http://purl.org/dc/terms/")) {
                outputMap.put(key.substring("http://purl.org/dc/terms/".length()), inputMap.get(key));
            }
            if (key.startsWith("http://inqool.cz/zdo/1.0/")) {
                outputMap.put(key.substring("http://inqool.cz/zdo/1.0/".length()), inputMap.get(key));
            }
        }
        return outputMap;
    }

    private QueryExecution queryDocuments(DocumentListRequest request) {
        //Prepare ordering
        String orderBy = request.getOrderBy();
        String orderByProp = null;
        if(orderBy != null) {   //hell begins
            switch(orderBy) {
                case "inventoryId":
                case "zdoType":
                    orderByProp = ZdoTerms.ns + orderBy;
                    break;
                case "title":
                    orderByProp = DCTerms.NS + orderBy;
                    break;
                case "created":
                    orderByProp = ZdoTerms.fedoraCreated.getURI();
                    break;
                case "lastModified":
                    orderByProp = ZdoTerms.fedoraLastModified.getURI();
                    break;
                default:
                    throw new RuntimeException("Unrecognized sort property");
            }
        }

        //Limit number of results and ensure ordering
        Integer limit = request.getLimit();
        Integer offset = request.getOffset();
        String limitPart1;
        String limitPart2;

        if(orderByProp == null) {
            limitPart1 = "  {\n" +
                    "    SELECT ?subject WHERE {\n";
            limitPart2 = "    }\n" +
                    "  ORDER BY ?subject\n";
        }
        else {
            limitPart1 = "  {\n" +
                    "    SELECT ?subject ?ord WHERE {\n" +
                    "      ?subject <" + orderByProp + "> ?ord.";
            limitPart2 = "    }\n" +
                    "  ORDER BY " + request.getOrderDir() + "(?ord)\n";
        }
        if(limit != null && limit > 0) {
            limitPart2 += "  LIMIT " + limit + "\n" +
                    "  OFFSET " + offset + "\n";
        }
        limitPart2 += "  }\n";

        //When listing original documents that can be made concept, do not list documents that have a concepted version already
        String state = request.getState();
        String noConceptedCondition = "";
        if ("original".equals(state)) {
            noConceptedCondition =
                    "  MINUS {\n" +
                            "    ?conceptedSubject <" + ZdoTerms.kdrObject + "> ?subject.\n" +
                            "    ?conceptedSubject <" + ZdoTerms.group.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ".\n" +
                            "  }\n";
        }

        String queryString = "SELECT ?subject ?property ?value WHERE {\n" +
                "  ?subject ?property ?value.\n" +
                limitPart1 +
                sparqlTools.createInventoryIdProxCondition(request.getInventoryId()) +
                sparqlTools.createDocumentStateCondition(state) +   //Filter documents by state
                sparqlTools.createBatchCondition(request.getBatch()) +
                sparqlTools.createOrganizationCondition(picketLinkAccess.getUsersOrganization().getName()) +
                sparqlTools.createOwnershipCondition(((ZdoUser) identity.getAccount()).getLoginName()) +
                sparqlTools.createEndBranchTypeCondition() +
                noConceptedCondition +
                limitPart2 +
                "}\n";
        if(orderByProp != null) {
            queryString += "ORDER BY " + request.getOrderDir() + "(?ord)\n";
        }
        return QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
    }

    //Query all documents with given inventoryId - concept, kdr, bach, externals
    private QueryExecution queryDocumentDetail(DocumentListRequest request) {

        String queryString = "SELECT ?subject ?property ?value WHERE {" +
                "  ?subject ?property ?value." +
                sparqlTools.createInventoryIdQuery(request.getInventoryId()) +
                sparqlTools.createAbovePageTypeCondition() +
                sparqlTools.createDocumentStateConditionIncludingOthers(request.getState()) +
                " }";
        return QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
    }

    @Transactional
    public void updateDocuments(List<Map<String, List<String>>> inputMapList) throws IOException {
        for (Map<String, List<String>> inputMap : inputMapList) {
            updateDocument(inputMap);
        }
    }

    @Transactional
    public void updateDocument(Map<String, List<String>> inputMap) throws IOException {
        List<String> idList = inputMap.remove("docId");
        String id = idList.get(0);
        ZdoModel model = store.get(store.createUrl(id));
        if (ZdoGroup.ZDO.name().equals(model.get(ZdoTerms.group))) {
            throw new RuntimeException("Can't update document that is in published state.");
        }
        if(!picketLinkAccess.getUsersOrganization().getName().equals(model.get(ZdoTerms.organization))) {
            throw new RuntimeException("Document belongs to different organization.");
        }
        if(!ZdoGroup.ZDO_CONCEPT.name().equals(model.get(ZdoTerms.group))) {
            throw new RuntimeException("Can only update concepts.");
        }
        if(!((ZdoUser) identity.getAccount()).getLoginName().equals(model.get(ZdoTerms.owner))) {
            throw new RuntimeException("Can only update owned documents.");
        }

        inputMap = fillNamespaces(inputMap);

        String docType = null;
        String docSubType = null;
        for (String prop : inputMap.keySet()) {
            //There could be 20 issues that have a document type updated, but we need to treat them specially to update it just once on their root
            if(!ZdoType.isRootCategory(model.get(ZdoTerms.zdoType))) {
                if(ZdoTerms.documentType.getURI().equals(prop)) {
                    List<String> propList = inputMap.get(prop);
                    if(!propList.isEmpty() && !propList.get(0).isEmpty()) {
                        docType = propList.get(0);
                        continue;
                    }
                }
                if(ZdoTerms.documentSubType.getURI().equals(prop)) {
                    List<String> propList = inputMap.get(prop);
                    if(!propList.isEmpty() && !propList.get(0).isEmpty()) {
                        docSubType = propList.get(0);
                        continue;
                    }
                }
            }

            model.removeAllValuesOfProperty(new PropertyImpl(prop));
            for (String val : inputMap.get(prop)) {

                //Where nklist values are used, make sure they are in our db
                /*//Allow only certain values for some props, unless we are in SCK and this is CHO, restriction does not apply there*/
                if(!((CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) && ZdoType.cho.name().equals(model.get(ZdoTerms.zdoType)))) {
                    /*allowOnlyOfficialListValues(prop, val);*/
                    makeSureNkListValueExists(prop, val);
                }

                if (prop.endsWith("_visibility") && "true".equals(val))
                    continue; //Visibility is true by default when false is not present, we can save space
                model.add(new PropertyImpl(prop), val);
            }
        }

        //If there are doc types to be changed, change them on parent
        if(docType != null || docSubType != null) {
            setDocTypesToRoot(docType, docSubType, model);
        }

        //Check if all required fields are filled, if yes, mark as ready to publish
        updateValidToPublish(model);
        store.update(model);
    }

    private void makeSureNkListValueExists(String prop, String val) {
        if(prop.equals("genre")) {
            nkListsAccess.makeSureGenreExists(val);
        }
        if(prop.equals("subject")) {
            nkListsAccess.makeSureTopicExists(val);
        }
        if(prop.equals("spatial")) {
            nkListsAccess.makeSureGeoExists(val);
        }
        if(prop.equals("temporal")) {
            nkListsAccess.makeSureChroExists(val);
        }
    }

    private void allowOnlyOfficialListValues(String prop, String val) {

        //Allow only data from official NK lists into some fields
        if(prop.equals("genre")) {
            if(!nkListsAccess.existsGenre(val)) {
                throw new RuntimeException("Attempt to save a value that is not in NK lists into genre.");
            }
        }
        if(prop.equals("subject")) {
            if(!nkListsAccess.existsTopic(val)) {
                throw new RuntimeException("Attempt to save a value that is not in NK lists into topic.");
            }
        }
        if(prop.equals("spatial")) {
            if(!nkListsAccess.existsGeo(val)) {
                throw new RuntimeException("Attempt to save a value that is not in NK lists into spatial.");
            }
        }
        if(prop.equals("temporal")) {
            if(!nkListsAccess.existsChro(val)) {
                throw new RuntimeException("Attempt to save a value that is not in NK lists into temporal.");
            }
        }
    }

    //Recursively goes to root and sets doc type and subtype there, remembers nodes traversed to not do them twice
    private void setDocTypesToRoot(String docType, String docSubType, ZdoModel model) throws IOException {
        String parentUrl = model.getParent();
        if(parentUrl != null) {
            if (!skipSet.contains(parentUrl)) {
                ZdoModel parent = store.get(parentUrl);
                if(ZdoType.isRootCategory(parent.get(ZdoTerms.zdoType))) {
                    if(docType != null) {
                        parent.replaceValueOfProperty(ZdoTerms.documentType, docType);
                    }
                    if(docSubType != null) {
                        parent.replaceValueOfProperty(ZdoTerms.documentSubType, docSubType);
                    }
                    updateValidToPublish(parent);
                    store.update(parent);
                }
                else {
                    setDocTypesToRoot(docType, docSubType, parent);
                }
                skipSet.add(parentUrl);
            }
        }
    }

    private void updateValidToPublish(ZdoModel model) {
        if (isFullyFilled(model)) {
            model.replaceValueOfProperty(ZdoTerms.validToPublish, "true");
        }
        else {
            model.removeAllValuesOfProperty(ZdoTerms.validToPublish);
        }
    }

    private boolean isFullyFilled(ZdoModel model) {
        boolean valid = true;
        String title = model.get(DCTerms.title);
        if(title == null || title.isEmpty()) {
            valid = false;
        }
        if(ZdoType.isRootCategory(model.get(ZdoTerms.zdoType))) {
            String docType = model.get(ZdoTerms.documentType);
            if(docType == null) {
                valid = false;
            }
            String docSubType = model.get(ZdoTerms.documentSubType);
            if(docSubType == null) {
                valid = false;
            }
        }
        return valid;
    }

    private Map<String, List<String>> fillNamespaces(Map<String, List<String>> inputMap) {
        Map<String, List<String>> outputMap = new HashMap<>();
        for (String propName : inputMap.keySet()) {
            String propNameToLookFor = propName;
            if (propName.contains("_visibility")) {
                propNameToLookFor = propName.substring(0, propName.indexOf("_visibility"));
            }
            if (DCTools.getDcTermList().contains(propNameToLookFor)) {
                outputMap.put("http://purl.org/dc/terms/" + propName, inputMap.get(propName));
            }
            if (propName.startsWith("custom") || ZdoTerms.getZdoPropNames().contains(propNameToLookFor)) {
                outputMap.put("http://inqool.cz/zdo/1.0/" + propName, inputMap.get(propName));
            }
        }
        return outputMap;
    }

    public int countDocuments(DocumentListRequest request) {
        String state = request.getState();
        String noConceptedCondition = "";
        if ("original".equals(state)) {
            noConceptedCondition =
                    "  MINUS {\n" +
                            "    ?conceptedSubject <" + ZdoTerms.kdrObject + "> ?subject.\n" +
                            "    ?conceptedSubject <" + ZdoTerms.group.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ".\n" +
                            "  }\n";
        }

        String queryString = "SELECT (COUNT(*) AS ?count) WHERE {\n" +
                sparqlTools.createInventoryIdProxCondition(request.getInventoryId()) +
                sparqlTools.createDocumentStateCondition(state) +   //Filter documents by state
                sparqlTools.createBatchCondition(request.getBatch()) +
                sparqlTools.createOrganizationCondition(picketLinkAccess.getUsersOrganization().getName()) +
                sparqlTools.createOwnershipCondition(((ZdoUser) identity.getAccount()).getLoginName()) +
                sparqlTools.createEndBranchTypeCondition() +
                noConceptedCondition +
                " }";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet rs = queryExecution.execSelect();
        if(!rs.hasNext()) {
            throw new RuntimeException("Failed when counting documents.");
        }
        return rs.next().getLiteral("count").getInt();
    }
}
