package com.inqool.dcap.office.api;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.integration.model.*;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.dto.ZdoDocumentBrief;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.office.api.resource.Document;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 19. 3. 2015.
 */
@RequestScoped
public class TriplestoreStuff {
    @Inject
    private EntityManager em;

    @Inject
    private SparqlTools sparqlTools;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    public List<ZdoDocumentBrief> fromPropertyMap(Map<String, Map<String, List<String>>> subjectMap) {
        List<ZdoDocumentBrief> result = new ArrayList<>();
        for(String modelUrl : subjectMap.keySet()) {
            ZdoDocumentBrief zdoDocumentBrief = new ZdoDocumentBrief();
            Map<String, List<String>> propertyMap = subjectMap.get(modelUrl);
            zdoDocumentBrief.setId(store.getOnlyIdFromUrl(modelUrl));
            zdoDocumentBrief.setInvId(propertyMap.get(ZdoTerms.inventoryId.getURI()).get(0));
            List<String> titles = propertyMap.get(DCTerms.title.getURI());
            if(titles == null || titles.isEmpty()) {
                zdoDocumentBrief.setTitle("Warning: Title missing.");
            }
            else {
                zdoDocumentBrief.setTitle(propertyMap.get(DCTerms.title.getURI()).get(0));
            }
            zdoDocumentBrief.setCreated(OffsetDateTime.parse(propertyMap.get(ZdoTerms.fedoraCreated.getURI()).get(0)).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
            zdoDocumentBrief.setModified(OffsetDateTime.parse(propertyMap.get(ZdoTerms.fedoraLastModified.getURI()).get(0)).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
            zdoDocumentBrief.setState(Document.zdoGroupToDocumentState(propertyMap.get(ZdoTerms.group.getURI()).get(0)));
            zdoDocumentBrief.setType(ZdoType.valueOf(propertyMap.get(ZdoTerms.zdoType.getURI()).get(0)));

            //Fill in validity for publishing
            List<String> validToPublishList = propertyMap.get(ZdoTerms.validToPublish.getURI());
            if(validToPublishList == null) {
                zdoDocumentBrief.setValidToPublish(false);
            }
            else {
                List<String> parentList = propertyMap.get(DCTerms.isPartOf.getURI());
                if(parentList != null) {
                    zdoDocumentBrief.setValidToPublish(checkParentValidity(parentList.get(0)));
                }
                else {
                    zdoDocumentBrief.setValidToPublish(true);
                }
            }

            //Fill in batch name
            List<String> batchIdList = propertyMap.get(ZdoTerms.batchId.getURI());
            if(batchIdList != null && batchIdList.size() == 1) {
                ZdoBatch batch = em.find(ZdoBatch.class, Integer.valueOf(batchIdList.get(0)));
                if(batch != null) {
                    zdoDocumentBrief.setBatchId(batch.getId());
                    zdoDocumentBrief.setBatchName(batch.getName());
                }
            }

            //Set if published versions exist - only meant for original documents
            if(propertyMap.containsKey(ZdoTerms.newestPublished.getURI())) {
                if(propertyMap.containsKey(ZdoTerms.owner.getURI())) {
                    zdoDocumentBrief.setPublishingState(Document.DocumentState.published);
                }
                else {
                    zdoDocumentBrief.setPublishingState(Document.DocumentState.unpublished);
                }
            }

            result.add(zdoDocumentBrief);
        }
        return result;
    }

    public boolean checkParentValidity(String url) {
        ZdoModel model = store.get(url);
        String validToPublish = model.get(ZdoTerms.validToPublish);
        if(validToPublish == null) {
            return false;
        }
        else {
            String parentUrl = model.get(DCTerms.isPartOf);
            return parentUrl == null || checkParentValidity(parentUrl);
        }
    }

    public boolean hasChildren(String docUrl) {
        String queryString = "SELECT ?subject WHERE {\n" +
                " ?subject <" + DCTerms.isPartOf.getURI() + "> <" + docUrl + ">.\n" +
                " } LIMIT 1";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        return resultSet.hasNext();
    }

    public int countDocChildren(String docUrl) {
        int count = 0;
        String queryString = "SELECT (COUNT(*) AS ?no) WHERE {\n" +
                " ?subject <" + DCTerms.isPartOf.getURI() + "> <" + docUrl + ">.\n" +
                sparqlTools.createDocumentStateCondition("all") +
                " }";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(resultSet.hasNext()) {
            count = resultSet.next().get("no").asLiteral().getInt();
        }
        return count;
    }

    public boolean documentIsConceptedAlready(ZdoModel model) {
        return getUrlOfConcepted(model) != null;
    }

    //If there exists Concept of document with same KDR UUID. return its URL, else null
    public String getUrlOfConcepted(ZdoModel model) {
        String queryString = "SELECT ?subject WHERE {\n" +
                "?subject <" + ZdoTerms.inventoryId.getURI() + "> " + ZdoTerms.stringConstantOf(model.get(ZdoTerms.inventoryId)) + ".\n" +
                "?subject <" + ZdoTerms.group.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ".\n" +
                " } LIMIT 1";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(resultSet.hasNext()) {
            return resultSet.next().get("subject").asResource().getURI();
        }
        return null;
    }

    public Map<String, Integer> getUrlAndBatchIdOfConcepted(ZdoModel model) {
        String queryString = "SELECT ?subject ?value WHERE {\n" +
                "?subject <" + ZdoTerms.inventoryId.getURI() + "> " + ZdoTerms.stringConstantOf(model.get(ZdoTerms.inventoryId)) + ".\n" +
                "?subject <" + ZdoTerms.group.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ".\n" +
                "?subject <" + ZdoTerms.batchId.getURI() + "> ?value.\n" +
                " } LIMIT 1";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(resultSet.hasNext()) {
            Map<String, Integer> resultMap = new HashMap<>();
            QuerySolution querySolution = resultSet.next();
            resultMap.put(querySolution.get("subject").asResource().getURI(), Integer.valueOf(querySolution.get("value").asLiteral().getString()));
            return resultMap;
        }
        return null;
    }

    public List<String> fetchDocUrlsForInvIds(List<String> docInvIds, ZdoGroup zdoGroup) {
        List<String> docIds = new ArrayList<>();
        if(docInvIds.isEmpty()) return docIds;

        //Search for right document group depending on batch state
        String stateCondition = "?subject <" + ZdoTerms.group.getURI() + "> " + ZdoTerms.stringConstantOf(zdoGroup.name()) + ".\n";

        //Construct conditions for inventory ids
        List<String> invIdConditions = new ArrayList<>();
        for(String docInvId : docInvIds) {
            invIdConditions.add("?subject <http://inqool.cz/zdo/1.0/inventoryId> " + ZdoTerms.stringConstantOf(docInvId) + ".");
        }
        //Put them together
        String queryString =
                "SELECT DISTINCT ?subject WHERE {\n" +
                        " { " + String.join(" } UNION \n { ", invIdConditions) + " }\n " +
                        stateCondition +
                        "} ORDER BY ?subject";

        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);

        //Go through results and list the doc ids
        ResultSet rs = queryExecution.execSelect();
        while (rs.hasNext()) {
            QuerySolution querySolution = rs.next();
            docIds.add(querySolution.getResource("subject").getURI());
        }
        return docIds;
    }

    public List<String> fetchUrlsOfTileableImageDescendants(String docUrl) {
        List<String> docUrls = new ArrayList<>();
        if(docUrl == null || docUrl.isEmpty()) return docUrls;

        String query = "SELECT ?subject WHERE {\n" +
                //first find pages - children of the node
                "?s <" + ZdoTerms.zdoType.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoType.page.name()) + ".\n" +
                "?s <http://purl.org/dc/terms/isPartOf> <" + docUrl + ">.\n" +
                //then children of those pages that are binary usercopy images
                "?subject <http://purl.org/dc/terms/isPartOf> ?s.\n" +
                "?subject <" + ZdoTerms.zdoType.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoType.binary.name()) + ".\n" +
                "?subject <" + ZdoTerms.fileType.getURI() + "> " + ZdoTerms.stringConstantOf(ZdoFileType.userCopy.name()) + ".\n" +
                "?subject <" + ZdoTerms.mimeType.getURI() + "> " + ZdoTerms.stringConstantOf("image/jpeg") + ".\n" +
                "}";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        ResultSet rs = queryExecution.execSelect();
        while (rs.hasNext()) {
            QuerySolution querySolution = rs.next();
            docUrls.add(querySolution.getResource("subject").getURI());
        }
        return docUrls;
    }

    public String findPublishedDocTitle(String invId) {
        List<String> docUrls = fetchDocUrlsForInvIds(Collections.singletonList(invId), ZdoGroup.ZDO);
        if(!docUrls.isEmpty()) {
            ZdoModel model = store.get(docUrls.get(0));
            return model.get(DCTerms.title);
        }
        return "Neznámý";
    }

    public List<String> findDocsThatShouldBePublished(String organization, int limit) {
        List<String> docIds = new ArrayList<>();
        String query = "SELECT ?subject WHERE {\n" +
                "  ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ".\n" +
                "  ?subject <" + ZdoTerms.organization + "> " + ZdoTerms.stringConstantOf(organization) + ".\n" +
                sparqlTools.createEndBranchTypeCondition() +
                "  MINUS { ?subject <" + ZdoTerms.newestPublished + "> ?published. }\n" +
                "  MINUS {\n" +   //Subtract issues whose periodicals are owned already
                "    ?subject <" + DCTerms.isPartOf + "> ?volume.\n" +
                "    ?volume <" + DCTerms.isPartOf + "> ?periodical.\n" +
                "    ?periodical <" + ZdoTerms.owner + "> ?anyOwner.\n" +
                "  }\n" +
                "  MINUS {\n" +   //Subtract docs that are owned already
                "    ?subject <" + ZdoTerms.owner + "> ?anyOwner1.\n" +
                "  }\n" +
                "  MINUS {\n" + //Subtract those that have a hint from kdr so as not to be published
                "    ?subject <" + ZdoTerms.publishHint + "> " + ZdoTerms.stringConstantOf(PublishHint.dontPublish.name()) + ".\n" +
                "  }\n" +
                "}\n" +
                "LIMIT " + limit;
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        ResultSet rs = queryExecution.execSelect();
        while (rs.hasNext()) {
            QuerySolution querySolution = rs.next();
            docIds.add(store.getOnlyIdFromUrl(querySolution.getResource("subject").getURI()));
        }
        return docIds;
    }
}
