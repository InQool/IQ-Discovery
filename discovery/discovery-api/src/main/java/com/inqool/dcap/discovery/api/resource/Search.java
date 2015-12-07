package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.discovery.api.core.StatsAsyncLayer;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.DocNode;
import com.inqool.dcap.discovery.api.dto.Facet;
import com.inqool.dcap.discovery.api.dto.SearchResponse;
import com.inqool.dcap.discovery.api.entity.DocWithInventoryId;
import com.inqool.dcap.discovery.api.request.SearchRequest;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 23. 2. 2015.
 */
@RequestScoped
@Path("/search")
public class Search {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "solr.endpoint.main")
    private String SOLR_MAIN_ENDPOINT;

    @Inject
    private DataStore store;

    @Inject
    private StatsAsyncLayer statsAsyncLayer;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response search(@Form SearchRequest searchRequest) throws SolrServerException {
        try {
            return Response.ok(searchInner(searchRequest)).build();
        }
        catch (Exception e) {
            logger.error("Solr search failed.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    public SearchResponse searchInner(SearchRequest searchRequest) throws SolrServerException {
        //        _query_:"+title:7 +zdotype:periodical" OR _query_:"{!parent which='zdotype:periodical'}title:7 -zdotype:periodical"
        String hardcoreQuery = "_query_:\"+(" + searchRequest.getQuery() + ") +(zdoType:periodical OR zdoType:monograph OR zdoType:cho OR zdoType:bornDigital)\" " +
                "OR _query_:\"{!parent which='zdoType:periodical'}+(" + searchRequest.getQuery() + ") AND (zdoType:volume OR zdoType:issue)\"";

/* to copy to solr:
        _query_:"+( ) +(zdoType:periodical OR zdoType:monograph OR zdoType:cho OR zdoType:bornDigital)" OR _query_:"{!parent which='zdoType:periodical'}+( ) -zdoType:periodical"
        _query_:"+(*:*) +(zdoType:periodical OR zdoType:monograph OR zdoType:cho OR zdoType:bornDigital)" OR _query_:"{!parent which='zdoType:periodical'} (*:*) AND (zdoType:volume OR zdoType:issue)"
*/

        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(hardcoreQuery);
        solrQuery.setStart(searchRequest.getStart());
        solrQuery.setRows(searchRequest.getMaxRecords());
        solrQuery.setFacet(searchRequest.isFacets());
        if (searchRequest.isFacets()) {
            solrQuery.setFacetLimit(20);
            solrQuery.setFacetSort("count");
            solrQuery.setFacetMinCount(1);
            solrQuery.setFacetMissing(false);
            if (searchRequest.getFacetQueries().isEmpty() && searchRequest.getFacetFields().isEmpty()) {
                solrQuery.addFacetQuery("datePublished:[NOW-1YEAR TO NOW]");
                solrQuery.addFacetQuery("datePublished:[NOW-6MONTH TO NOW]");
                solrQuery.addFacetQuery("datePublished:[NOW-1MONTH TO NOW]");
                solrQuery.addFacetQuery("datePublished:[NOW-7DAY TO NOW]");

                solrQuery.addFacetField("documentType");
                solrQuery.addFacetField("documentSubType");
                solrQuery.addFacetField("creatorStr");
                solrQuery.addFacetField("organization");
                solrQuery.addFacetField("language");
                solrQuery.addFacetField("spatial");
                solrQuery.addFacetField("temporal");
                solrQuery.addFacetField("subject");
                solrQuery.addFacetField("type");
                solrQuery.addFacetField("yearStart");
                /*solrQuery.addNumericRangeFacet("yearStart", -10000, LocalDateTime.now().getYear(), 1);*/
            } else {
                searchRequest.getFacetQueries().forEach(solrQuery::addFacetQuery);
                searchRequest.getFacetFields().forEach(solrQuery::addFacetField);
            }
        }

        if (searchRequest.getSort() != null) {
            solrQuery.setSort(SolrQuery.SortClause.create(searchRequest.getSort(), searchRequest.getSortDir()));
        }
        List<String> filterQueries = searchRequest.getFilterQueries();
        if (!filterQueries.isEmpty()) {
            solrQuery.setFilterQueries(filterQueries.stream().toArray(String[]::new));
        }

        solrQuery.setFields("inventoryId", "imgThumb", "title", "creator", "publisher", "created", "temporal", "spatial", "zdoType");

        //Query the Solr
        QueryResponse rsp = server.query(solrQuery);
        SolrDocumentList docs = rsp.getResults();

//        postprocessResults(docs);

        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setSearchResults(docs);
        searchResponse.setNumResults(docs.getNumFound());
        searchResponse.setFacets(reshovelFacets(rsp));
        return searchResponse;
    }

    @GET
    @Path("/detail")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public SolrDocumentList searchDetail(@Form SearchRequest searchRequest) throws SolrServerException {

        String hardcoreQuery = "(" + searchRequest.getQuery() + ") AND _root_:\"" + searchRequest.getParentId() + "\" -zdoType:periodical";

        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(hardcoreQuery);
        solrQuery.setStart(searchRequest.getStart());
        solrQuery.setRows(searchRequest.getMaxRecords());
        if(searchRequest.getSort() != null) {
            solrQuery.setSort(SolrQuery.SortClause.create(searchRequest.getSort(), searchRequest.getSortDir()));
        }
        QueryResponse rsp = server.query(solrQuery);
        SolrDocumentList docs = rsp.getResults();
        postprocessResults(docs);
        return docs;
    }
    //"{!child of=id:" + searchRequest.getParentId() + "}" + searchRequest.getQuery();
//    ({!child of=zdoType:"periodical"}id:"http://localhost:8380/fedora/rest/49/13/a8/e3/4913a8e3-54fa-4a16-8302-bf7292445621") AND created:"1931"

    /**
     * Just to draw tree of children of one root.
     * @param parentId .
     * @return tree
     * @throws SolrServerException
     */
    @GET
    @Path("/tree")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Collection<DocNode> searchTree(@QueryParam("parentId") String parentId) throws SolrServerException {
        if(parentId == null || parentId.isEmpty()) return Collections.emptyList();

        String allOfRootQuery = "_root_:\"" + parentId + "\"";

        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(allOfRootQuery);
        solrQuery.setRows(10000);
        solrQuery.setFields("id", "title", "isPartOf", "inventoryId", "created");
        QueryResponse rsp = server.query(solrQuery);
        SolrDocumentList docs = rsp.getResults();

        //This puts volumes in the map for easy access
        Map<String, DocNode> docNodeMap = new HashMap<>();
        for(SolrDocument doc : docs) {
            String parentUrl = (String) doc.getFirstValue("isPartOf");
            if(parentUrl != null) {
                Object createdObj = doc.getFirstValue("created");
                String created = null;
                if(createdObj != null) {
                    created = (String) createdObj;
                }
                if(parentUrl.equals(parentId)) {
                    DocNode docNode = new DocNode(
                            (String) doc.get("id"),
                            (String) doc.getFirstValue("title"),
                            (String) doc.getFirstValue("inventoryId"),
                            created
                    );
                    docNodeMap.put((String) doc.get("id"), docNode);
                }
            }
        }

        //This puts issues under respective volumes
        for(SolrDocument doc : docs) {
            String parentUrl = (String) doc.getFirstValue("isPartOf");
            if(parentUrl != null && !parentUrl.equals(parentId)) {
                if(docNodeMap.containsKey(parentUrl)) {
                    DocNode volumeNode = docNodeMap.get(parentUrl);
                    DocNode docNode = new DocNode((String) doc.get("id"), (String) doc.getFirstValue("title"), (String) doc.getFirstValue("inventoryId"));
                    volumeNode.getChildren().add(docNode);
                }
            }
        }

        List<DocNode> resultList = new ArrayList<>();
        resultList.addAll(docNodeMap.values());
        sortRecur(resultList);
        return resultList;
    }

    private static void sortRecur(List<DocNode> list) {
        list.sort(Comparator.<DocNode>naturalOrder());
        list.forEach(element -> sortRecur(element.getChildren()));
    }

    /**
     * Gets the exact one document
     * @param invId .
     * @return .
     * @throws SolrServerException
     */
    @GET
    @Path("/invId")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response searchDetailByInvId(@QueryParam("id") String invId) throws SolrServerException {
        if(invId == null || invId.isEmpty()) return null;

        String invIdQuery = "inventoryId:\"" + invId + "\"";

        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(invIdQuery);
        solrQuery.setRows(1);
        QueryResponse rsp = server.query(solrQuery);
        SolrDocumentList docs = rsp.getResults();
        postprocessResults(docs);

        if(docs.size() != 1) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        statsAsyncLayer.incrementDocViews((String) docs.get(0).get("inventoryId"));
        return Response.ok(docs.get(0)).build();
    }

    @GET
    @Path("/mlt")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response findMoreLikeThis(@QueryParam("id") String invId) throws SolrServerException {
        if(invId == null || invId.isEmpty()) return null;

        String invIdQuery = "inventoryId:\"" + invId + "\"";

        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler("/mlt");
        solrQuery.setQuery(invIdQuery);
        solrQuery.setRows(5);
        solrQuery.setFields("title", "inventoryId", "zdoType");
        QueryResponse rsp = server.query(solrQuery);
        SolrDocumentList docs = rsp.getResults();
        ListIterator<SolrDocument> iter = docs.listIterator();
        while(iter.hasNext()) {
            SolrDocument doc = iter.next();
            //Remove docs that are children of this doc, or this doc
            Object foundInvIdObj = doc.get("inventoryId");
            if(foundInvIdObj != null) {
                String foundInvId = (String) foundInvIdObj;
                if(foundInvId.contains(invId)) {
                    iter.remove();
                    continue;
                }
            }
            //Remove non-root docs
            Object zdoTypeObj = doc.get("zdoType");
            if(zdoTypeObj != null) {
                String zdoType = (String) zdoTypeObj;
                if(!ZdoType.isRootCategory(zdoType)) {
                    iter.remove();
                    continue;
                }
            }
        }

        return Response.ok(docs).build();
    }

    //Only allow published docs to continue
    private void postprocessResults(SolrDocumentList docs) {
        ListIterator<SolrDocument> iter = docs.listIterator();
        while(iter.hasNext()) {
            SolrDocument doc = iter.next();
            Object allowContentPubliclyObj = doc.get("allowContentPublicly");
            if(allowContentPubliclyObj == null || !((boolean) allowContentPubliclyObj)) {
                disallowContent(doc);
                continue;
            }

            Object publishFromObj = doc.get("publishFrom");
            Object publishToObj = doc.get("publishTo");
            Instant now = Instant.now();
            if(publishFromObj != null) {
                Instant from = Instant.ofEpochMilli(((Date) publishFromObj).getTime());
                if(now.isBefore(from)) {
                    disallowContent(doc);
                    continue;
                }
            }
            if(publishToObj != null) {
                Instant to = Instant.ofEpochMilli(((Date) publishToObj).getTime());
                if(now.isAfter(to)) {
                    disallowContent(doc);
                    continue;
                }
            }
            Object allowPdfExportObj = doc.get("allowPdfExport");
            if(allowPdfExportObj == null || !((boolean) allowPdfExportObj)) {
                doc.remove("pdfId");
            }
            Object allowEpubExportObj = doc.get("allowEpubExport");
            if(allowEpubExportObj == null || !((boolean) allowEpubExportObj)) {
                doc.remove("epubId");
            }
        }
    }

    private void disallowContent(SolrDocument doc) {
        doc.remove("thumbIds");
        doc.remove("imageIds");
        doc.remove("content");
        doc.remove("pdfId");
        doc.remove("epubId");
    }

    //Put facets from solr response to our serializable dto
    private List<Facet> reshovelFacets(QueryResponse rsp) {
        List<Facet> facets = new ArrayList<>();

        //Field facets
        List<FacetField> facetFieldList = rsp.getFacetFields();
        if(facetFieldList != null) {
            for (FacetField facetField : facetFieldList) {
                Facet facet = new Facet();
                facet.setName(facetField.getName());
                for (FacetField.Count count : facetField.getValues()) {
                    if (count.getName() == null) {
                        continue;   //do not show facet of how many docs have value empty
                        /*if (count.getCount() >= 1) {
                            facet.getHitMap().put("Neznámo", count.getCount());
                        }*/
                    } else {
                        facet.getHitMap().put(count.getName(), count.getCount());
                    }
                }
                facets.add(facet);
            }
        }

        //Query facets
        Map<String, Integer> facetQueryMap = rsp.getFacetQuery();
        Map<String, Facet> facetIndex = new HashMap<>();
        if(facetQueryMap != null) {
            for(Map.Entry<String, Integer> entry : facetQueryMap.entrySet()) {
                String[] parts = entry.getKey().split(":");
                if(parts.length != 2) {
                    logger.error("Solr query facet didn't have 2 parts.");
                    continue;
                }
                String fieldName = parts[0];
                Facet facet = facetIndex.get(fieldName);
                if(facet == null) {
                    facet = new Facet();
                    facet.setName(fieldName);
                    facets.add(facet);
                    facetIndex.put(fieldName, facet);
                }
                facet.getHitMap().put(parts[1], Long.valueOf(entry.getValue()));
            }
        }

        //So far unused
        //Numeric range facets
        List<RangeFacet> rangeFacets = rsp.getFacetRanges();
        if(rangeFacets != null) {
            for(RangeFacet rangeFacet : rangeFacets) {
                Facet facet = new Facet();
                facet.setName(rangeFacet.getName());
                for(RangeFacet.Count count : (List<RangeFacet.Count>) rangeFacet.getCounts()) {
                    if (count.getValue() == null) {
                        if (count.getCount() >= 1) {
                            facet.getHitMap().put("Neznámo", (long) count.getCount());
                        }
                    } else {
                        facet.getHitMap().put(count.getValue(), (long) count.getCount());
                    }
                }
                facets.add(facet);
            }
        }
        return facets;
    }

    public SolrDocumentList searchByInvIdList(List<? extends DocWithInventoryId> docInvIdList) throws SolrServerException {
        String query = "";
        for (DocWithInventoryId docWithInventoryId : docInvIdList) {
            if(!query.equals("")) {
                query += " OR ";
            }
            query += "inventoryId:\"" + docWithInventoryId.getDocInvId() + "\"";
        }

        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        solrQuery.setStart(0);
        solrQuery.setRows(1000);
        QueryResponse rsp = server.query(solrQuery);
        return rsp.getResults();
    }
}
