/*
 * TestResource.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.inqool.dcap.office.api.resource;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.DebugDTO;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.exception.FailedToLoadException;
import com.inqool.dcap.integration.exception.FailedToParseException;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.oai.harvester.OaiHarvester;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.integration.service.SparqlOnDemandIndexer;
import com.inqool.dcap.office.api.HarvestingScheduler;
import com.inqool.dcap.office.indexer.indexer.SolrBulkIndexer;
import com.inqool.dcap.security.ZdoRoles;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@RequestScoped
@Path("/debug")
public class Debug {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    @ConfigProperty(name = "fedora.endpoint")
    private String FEDORA_ENDPOINT;

    @Inject
    private SparqlTools sparqlTools;

    @Inject
    private SparqlOnDemandIndexer sparqlOnDemandIndexer;

    @Inject
    private SolrBulkIndexer solrBulkIndexer;

    @Inject
    private OaiHarvester harvester;
    @Inject
    private HarvestingScheduler harvestingScheduler;

    @Path("/harvest")
    @GET
    public String testharv() throws FailedToLoadException, FailedToParseException, IOException {
/*        OaiSource oaiSource = new OaiSource();
//        oaiSource.setUrl("http://svk7.svkkl.cz/i2/i2.ws.oai.cls");
        oaiSource.setUrl("http://www.kfbz.cz/oainew/");
        oaiSource.setShortcut("KKFB");
//        oaiSource.setSet("KLCKDATE");
        oaiSource.setLastHarvested(LocalDateTime.now().minusMonths(2));
        harvester.harvestSource(oaiSource);*/

        harvestingScheduler.finish(null);

        return "";
    }

/*    @Path("/autopublish")
    @GET
    public String autopublish() throws FailedToLoadException, FailedToParseException, IOException {
        autoPublishScheduler.finish(null);
        return "ok";
    }*/

    @Path("/fix")
    @GET
    public String test2() throws FailedToLoadException, FailedToParseException, IOException {
        ZdoModel model = new ZdoModel(store.createUrl("d97f9883-ff12-4ae8-b05a-c24f83a2e84aaa"), new FileInputStream("D:\\DLC\\aaMV_MK_Film~1_01_0001.mp4"));
        model.add(ZdoTerms.mimeType, "video/mp4");
        store.update(model);
//reformat additional metadata
/*        String query = "SELECT distinct ?s WHERE {\n" +
                " ?s <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.KDR.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                "}";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        ResultSet resultSet = queryExecution.execSelect();
        while(resultSet.hasNext()) {
            String url = resultSet.next().get("s").asResource().getURI();
            ZdoModel model = store.get(url);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
                HashMap<String, String> metadataMap = objectMapper.readValue(model.get(ZdoTerms.additionalMetadata), typeRef);
                HashMap<String, List<String>> resultMap = new HashMap<>();
                for (Map.Entry<String, String> stringStringEntry : metadataMap.entrySet()) {
                    List<String> list = new ArrayList<>();
                    list.add(stringStringEntry.getValue());
                    resultMap.put(stringStringEntry.getKey(), list);
                }
                model.replaceValueOfProperty(ZdoTerms.additionalMetadata, (new ObjectMapper()).writeValueAsString(resultMap));
            } catch (Exception e) {
                logger.debug("not in that format.", e);
            }
            store.update(model);
        }*/
        return "";
    }

    @Path("/check")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response check() throws FailedToLoadException, FailedToParseException, IOException {
        List<DebugDTO> fedoraObjects = dumpFedoraBrief();
        List<DebugDTO> triplestoreObjects = dumpTriplestore();

        if(fedoraObjects.equals(triplestoreObjects)) {
            return Response.ok("OK").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("bad").build();
    }

/*    @Path("/repairTriplestore")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response repairTriplestore() throws FailedToLoadException, FailedToParseException, IOException {
        List<String> allUrlsList = store.getAll();

        String query = "SELECT DISTINCT ?s WHERE {\n" +
                " ?s ?p ?v.\n" +
                "}";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        queryExecution.execSelect();

        List<DebugDTO> fedoraObjects = dumpFedoraBrief();

        fedoraObjects;

        return Response.ok(dumpTriplestore()).build();
    }*/

    //Reindexes all Fedora data to triplestore
    //Triplestore should be empty before calling this
    @Path("/reindexTriplestore")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reindexTriplestore() throws FailedToLoadException, FailedToParseException, IOException {
        //Get urls of models in Fedora
        logger.info("Fetching model list from Fedora.");
        List<String> allUrlsList = store.getAll();
        logger.debug("Reindexing models, total " + allUrlsList.size());
        int counter = 0;
        List<ZdoModel> toUpdate = new ArrayList<>();
        for (String url : allUrlsList) {
            try {
                ZdoModel model = store.get(url);

                //Deal with time
                model.removeAllValuesOfProperty(ZdoTerms.fedoraLastModified);
                if (ZdoType.isAbovePageCategory(model.get(ZdoTerms.zdoType))) {
                    model.add(model.getSubject(), ZdoTerms.fedoraLastModified, OffsetDateTime.now().toString(), XSDDatatype.XSDdateTime);
                    if (model.get(ZdoTerms.fedoraCreated) == null) {
                        model.add(model.getSubject(), ZdoTerms.fedoraCreated, OffsetDateTime.now().toString(), XSDDatatype.XSDdateTime);
                    }
                } else {
                    model.removeAllValuesOfProperty(ZdoTerms.fedoraCreated);
                }

                toUpdate.add(model);

                //Log progress every 100 models
                if (++counter % 100 == 0) {
                    logger.debug("Reindexing models, " + counter + " of " + allUrlsList.size() + " done.");
                }

                //Write them to triplestore using batches of size 1000
                if (toUpdate.size() >= 1000) {
                    logger.debug("Updating triplestore.");
                    sparqlOnDemandIndexer.bulkUpdate(toUpdate);
                    toUpdate.clear();
                }
            }
            catch(Exception e) {
                logger.error("Failed to load one model to triplestore.", e);
            }
        }
        logger.debug("Updating triplestore.");
        sparqlOnDemandIndexer.bulkUpdate(toUpdate);

        logger.debug("Triplestore reindexing finished.");
        return Response.ok().build();
    }

    //Reindexes published data to Solr
    //Solr should be empty before calling this
    //Triplestore and Fedora should contain correct data
    @Path("/reindexSolr")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reindexSolr() throws FailedToLoadException, FailedToParseException, IOException {
        //Get all published root models
        logger.debug("Fetching root document urls from triplestore.");
        String query = "SELECT DISTINCT ?subject WHERE {\n";
        query += sparqlTools.createDocumentStateCondition("published");
        query += sparqlTools.createRootTypeCondition();
        query += "}";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        ResultSet rs = queryExecution.execSelect();
        logger.debug("Root documents fetched.");
        int counter = 0;
        while (rs.hasNext()) {
            String url = rs.next().get("subject").asResource().getURI();
            logger.debug("Indexing another root model to solr. #" + ++counter);
            solrBulkIndexer.updateUri(url);
        }
        return Response.ok().build();
    }

    @Path("/dumpFedora")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response dumpFedoraRS() throws FailedToLoadException, FailedToParseException, IOException {
        return Response.ok(dumpFedoraBrief()).build();
    }

    @Path("/dumpTriplestore")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response dumpTriplestoreRS() throws FailedToLoadException, FailedToParseException, IOException {
        return Response.ok(dumpTriplestore()).build();
    }

    public List<DebugDTO> dumpFedoraBrief() {
        Map<String, DebugDTO> nodeIndex = new HashMap<>();
        List<DebugDTO> rootList = new ArrayList<>();
        Map<String, List<DebugDTO>> cache = new HashMap<>();

        List<String> allUrlsList = store.getAll();
        for(String url : allUrlsList) {
            ZdoModel model = store.get(url);
            store.getOnlyIdFromUrl(model.getUrl());

            DebugDTO debugDTO = new DebugDTO();
            debugDTO.setUrl(model.getUrl());
            debugDTO.setGroup(model.get(ZdoTerms.group));
            debugDTO.setInvId(model.get(ZdoTerms.inventoryId));
            debugDTO.setType(model.get(ZdoTerms.zdoType));

            String parentUrl = model.getParent();
            if(parentUrl != null) {
                String parentId = store.getOnlyIdFromUrl(parentUrl);
                if(!cache.containsKey(parentId)) {
                    cache.put(parentId, new ArrayList<>());
                }
                cache.get(parentId).add(debugDTO);
            } else {
                rootList.add(debugDTO);
            }
            nodeIndex.put(store.getOnlyIdFromUrl(model.getUrl()), debugDTO);
        }

        for (Map.Entry<String, List<DebugDTO>> entry : cache.entrySet()) {
            String parentId = entry.getKey();
            if (nodeIndex.containsKey(parentId)) {
                nodeIndex.get(parentId).getChildren().addAll(entry.getValue());
            } else {
                logger.error("Orphan found!" + entry.getValue().get(0).getUrl() + " missing " + entry.getKey());
                entry.getValue().forEach(dto -> dto.setUrl(dto.getUrl() + " MISSING PARENT"));
                rootList.addAll(entry.getValue());
            }
        }
        sortListTree(rootList);
        return rootList;
    }

    public List<DebugDTO> dumpTriplestore() {
        Map<String, DebugDTO> nodeIndex = new HashMap<>();
        List<DebugDTO> rootList = new ArrayList<>();
        Map<String, List<DebugDTO>> cache = new HashMap<>();

        String query = "SELECT ?subject ?property ?value WHERE {\n" +
                " ?subject ?property ?value.\n" +
                " MINUS {" +
                "   ?subject <" + ZdoTerms.zdoType.getURI() + "> \"binary\"^^<http://www.w3.org/2001/XMLSchema#string>." +
                " }" +
                " MINUS {" +
                "   ?subject <" + ZdoTerms.zdoType.getURI() + "> \"page\"^^<http://www.w3.org/2001/XMLSchema#string>." +
                " }" +
                "}";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        Map<String, Map<String, List<String>>> allMap = sparqlTools.queryExecutionToPropertyMap(queryExecution);
        for (String subject : allMap.keySet()) {

            Map<String, List<String>> propertyMap = allMap.get(subject);

            DebugDTO debugDTO = new DebugDTO();
            debugDTO.setUrl(subject);
            List<String> groupList = propertyMap.get(ZdoTerms.group.getURI());
            if(groupList != null) {
                debugDTO.setGroup(groupList.get(0));
            }
            List<String> invIdList = propertyMap.get(ZdoTerms.inventoryId.getURI());
            if(invIdList != null) {
                debugDTO.setInvId(invIdList.get(0));
            }
            List<String> typeList = propertyMap.get(ZdoTerms.zdoType.getURI());
            if(typeList != null) {
                debugDTO.setType(typeList.get(0));
            }

            List<String> isPartOfList = propertyMap.get(DCTerms.isPartOf.getURI());
            if(isPartOfList != null) {
                String parentUrl = isPartOfList.get(0);
                String parentId = store.getOnlyIdFromUrl(parentUrl);
                if(!cache.containsKey(parentId)) {
                    cache.put(parentId, new ArrayList<>());
                }
                cache.get(parentId).add(debugDTO);
            }
            else {
                rootList.add(debugDTO);
            }
            nodeIndex.put(store.getOnlyIdFromUrl(subject), debugDTO);
        }

        for (Map.Entry<String, List<DebugDTO>> entry : cache.entrySet()) {
            String parentId = entry.getKey();
            if (nodeIndex.containsKey(parentId)) {
                nodeIndex.get(parentId).getChildren().addAll(entry.getValue());
            } else {
                logger.error("Orphan found!" + entry.getValue().get(0).getUrl() + " missing " + entry.getKey());
                entry.getValue().forEach(dto -> dto.setUrl(dto.getUrl() + " MISSING PARENT"));
                rootList.addAll(entry.getValue());
            }
        }
        sortListTree(rootList);
        return rootList;
    }

    private void sortListTree(List<DebugDTO> list) {
        list.sort(Comparator.<DebugDTO>reverseOrder());
        list.forEach(element -> sortListTree(element.getChildren()));
    }

    @Path("/echo")
    @GET
    public Response echo() {
        return Response.ok().build();
    }
}
