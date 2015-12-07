/**
 * Copyright 2014 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inqool.dcap.office.indexer.indexer;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.DCTools;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.YearNormalizer;
import com.inqool.dcap.common.DocumentTypeAccess;
import com.inqool.dcap.common.dto.PdfCreatorDto;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.*;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.core.OrganizationSettingsAccess;
import com.inqool.dcap.office.api.core.PortalSettingsAccess;
import com.inqool.dcap.office.api.dto.ModelTreeNode;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoOrganization;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;

import javax.ejb.AsyncResult;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Solr Indexer implementation that adds information to a
 * Solr index server.
 *
 * @author ajs6f
 * @author yecao
 * @author Matus Zamborsky (inQool)
 * @author Lukas Jane (inQool) 19. 3. 2015.
 */
@RequestScoped
public class SolrBulkIndexer {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    @ConfigProperty(name = "solr.endpoint.main")
    private String SOLR_MAIN_ENDPOINT;

    @Inject
    @ConfigProperty(name = "solr.endpoint.suggester")
    private String SOLR_SUGGESTER_ENDPOINT;

    @Inject
    @ConfigProperty(name = "ip.endpoint")
    private String IP_ENDPOINT;

    @Inject
    private SparqlTools sparqlTools;

    @Inject
    private DocumentTypeAccess documentTypeAccess;

    @Inject
    private EPUBCreator epubCreator;

    @Inject
    private OrganizationSettingsAccess organizationSettingsAccess;

    @Inject
    private PortalSettingsAccess portalSettingsAccess;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    private Map<String, String> orgNameMapping = new HashMap<>();

    private List<SolrInputDocument> dataForSuggester = new ArrayList<>();

    // TODO make index-time boost somehow adjustable, or something
    private static final Long INDEX_TIME_BOOST = 1L;

    private Set<String> allowableTypes = new HashSet<>(Arrays.asList(    //index only these types
            ZdoType.cho.name(),
            ZdoType.monograph.name(),
            ZdoType.periodical.name(),
            ZdoType.volume.name(),
            ZdoType.issue.name(),
            ZdoType.bornDigital.name(),
            ZdoType.page.name()
    ));

    public void update(ModelTreeNode data) throws IOException {
        dataForSuggester.clear();
        SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);

        fetchOrgNames();

        String fedoraId = store.getOnlyIdFromUrl(data.getModel().getUrl());
        //First remove old doc
        try {
            //first delete children
            //not needed, if _root_ is indexed, children are deleted with parent
            final UpdateResponse resp0 = server.deleteByQuery("_root_:\"" + fedoraId + "\"");
            if (resp0.getStatus() == 0) {
                logger.debug("Remove request was successful for children of: {}", fedoraId);
            } else {
                logger.error("Remove request has error, code: {} for pid: {}", resp0.getStatus(), fedoraId);
                return;
            }

            //then delete the doc itself
            final UpdateResponse resp1 = server.deleteById(fedoraId);
            if (resp1.getStatus() == 0) {
                logger.debug("Remove request was successful for: {}", fedoraId);
            } else {
                logger.error("Remove request has error, code: {} for pid: {}", resp1.getStatus(), fedoraId);
                return;
            }

            //Also delete suggester data belonging to the document root
            SolrServer suggesterServer = new HttpSolrServer(SOLR_SUGGESTER_ENDPOINT);
            final UpdateResponse resp2 = suggesterServer.deleteByQuery("belongsTo:\"" + fedoraId + "\"");
            if (resp2.getStatus() == 0) {
                logger.debug("Remove request was successful for suggester data of: {}", fedoraId);
            } else {
                logger.error("Remove request for suggester data has error, code: {} for pid: {}", resp2.getStatus(), fedoraId);
                return;
            }
        } catch (final SolrServerException | IOException e) {
            logger.error("Delete Exception: {}", e);
            throw new RuntimeException(e);
        }

        //Recursively dig to fedora and triplestore to construct whole solr document hierarchy
        SolrInputDocument solrInputDocument = recursivelyIndex(data);
        if(solrInputDocument == null) return;   //this was probably a delete request, skip inserting
        String solrDoc = solrInputDocument.toString();
        if(solrDoc.length() > 500) {
            solrDoc = solrDoc.substring(0, 500);
        }
        logger.debug("Created SolrInputDocument: {}", solrDoc);

        //Then insert new docs
        try {
            final UpdateResponse resp = server.add(solrInputDocument);
            if (resp.getStatus() == 0) {
                logger.debug("Update request was successful for: {}", fedoraId);
            } else {
                logger.error("Update request returned error code: {} for identifier: {}", resp.getStatus(), fedoraId);
            }
            logger.debug("Received result from Solr request.");
        } catch (final SolrServerException | IOException e) {
            logger.error("Update exception: {}!", e);
            throw new RuntimeException(e);
        }

        //And update also suggester data
        try {
            //For data for suggester, add info what root document they belong to, so that they can be also unindexed later with the document
            for (SolrInputDocument solrInputDoc : dataForSuggester) {
                solrInputDoc.addField("belongsTo", fedoraId);
            }
            SolrServer suggesterServer = new HttpSolrServer(SOLR_SUGGESTER_ENDPOINT);
            final UpdateResponse resp = suggesterServer.add(dataForSuggester);
            if (resp.getStatus() == 0) {
                logger.debug("Updating solr suggester data successful.");
            } else {
                logger.error("Update request for solr suggester data returned error code: {} for identifier: {}", resp.getStatus(), fedoraId);
            }
        } catch (Exception e) {
            logger.error("Error updating solr sugesster data.", e);
        }
    }

    private SolrInputDocument recursivelyIndex(final ModelTreeNode data) throws IOException {
        ZdoModel model;

        model = data.getModel();

        if (model == null) {
            return null;
        }

//        if (!model.isIndexable()) {
//            logger.debug("Resource: {} retrieved without indexable type.", uri);
//            return null;
//        }
        logger.debug("Resource: {} retrieved with indexable type.", store.removeTransactionFromUrl(model.getUrl()));

        if(!allowableTypes.contains(model.get(ZdoTerms.zdoType))) {
            return null;
        }

        if(!ZdoGroup.ZDO.name().equals(model.get(ZdoTerms.group))) {
            logger.info("Not indexing this document as it is not published.");
            return null;
        }

        final SolrInputDocument inputDoc = modelToSolrInputDoc(model);

//        inputDoc.addField("datePublished", OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME));
        String datePublished = model.get(ZdoTerms.datePublished);
        if(datePublished != null) { //If reindexing, we just read data about when it was originally published from Fedora
            inputDoc.addField("datePublished", datePublished);
        }
        else {
            datePublished = LocalDateTime.now().atZone(ZoneOffset.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            inputDoc.addField("datePublished", datePublished);  //solr needs UTC time
            ZdoModel patchmodel = new ZdoModel();
            patchmodel.setUrl(model.getUrl());
            patchmodel.add(ZdoTerms.datePublished, datePublished);
            store.patchMetadata(patchmodel);
        }
        //Get all children's uris, parse them recursively, and add them to result
        //If we are an almost-leaf node, also search for children bound on the original object
        String originalObjectUrl = model.get(ZdoTerms.kdrObject);
        if(!ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) {
            for(ModelTreeNode child : data.getChildren()) {
                SolrInputDocument childDoc = recursivelyIndex(child);
                if(childDoc != null) {
                    inputDoc.addChildDocument(childDoc);
                }
            }
        }
        else {  //we are end branch category
            //Treat born digital documents differently as they don't have pages but whole PDF
            if(ZdoType.bornDigital.name().equals(model.get(ZdoTerms.zdoType))) {
                //Retrieve the usercopy - PDF
                String queryString = "SELECT ?userCopy ?thumb WHERE {\n" +
                        "?userCopy <http://purl.org/dc/terms/isPartOf> <" + originalObjectUrl + ">.\n" +
                        "?userCopy <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.binary.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?userCopy <" + ZdoTerms.fileType.getURI() + "> \"" + ZdoFileType.userCopy.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "}";
                QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
                ResultSet resultSet = queryExecution.execSelect();
                if (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.next();
                    String userCopyUrl = querySolution.getResource("userCopy").getURI();
                    inputDoc.addField("pdfId", store.getOnlyIdFromUrl(userCopyUrl));
                }
                else {
                    throw new RuntimeException("Damn this pdf has no pdf or thumbnail.");
                }
            }
            else {  //Other than born-digital branch end node
                //These are to sort pages based on their index
                SortedMap<Integer, String> imageMap = new TreeMap<>();
                SortedMap<Integer, String> thumbMap = new TreeMap<>();
                SortedMap<Integer, String> txtMap = new TreeMap<>();
                SortedMap<Integer, String> altoMap = new TreeMap<>();

                String videoUrl = null;

                //Retrieve image, thumbnail and ocr text info
                String queryString = "SELECT ?pageIndex ?userCopy ?ucMime ?thumb ?txt ?alto WHERE {\n" +
                        //first find pages - children of the node
                        "?page <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.page.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?page <http://purl.org/dc/terms/isPartOf> <" + originalObjectUrl + ">.\n" +
                        "?page <" + ZdoTerms.pageIndex.getURI() + "> ?pageIndex.\n" +
                        "OPTIONAL {\n" +
                        //then children of those pages that are binary usercopy images
                        "?userCopy <http://purl.org/dc/terms/isPartOf> ?page.\n" +
                        "?userCopy <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.binary.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?userCopy <" + ZdoTerms.fileType.getURI() + "> \"" + ZdoFileType.userCopy.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?userCopy <" + ZdoTerms.mimeType.getURI() + "> ?ucMime.\n" +
                        "}\nOPTIONAL {\n" +
                        //and their thumbnails
                        "?thumb <http://purl.org/dc/terms/isPartOf> ?page.\n" +
                        "?thumb <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.binary.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?thumb <" + ZdoTerms.fileType.getURI() + "> \"" + ZdoFileType.thumb.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "}\nOPTIONAL {\n" +
                        //and also children of those pages that are binary text
                        "?txt <http://purl.org/dc/terms/isPartOf> ?page.\n" +
                        "?txt <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.binary.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?txt <" + ZdoTerms.fileType.getURI() + "> \"" + ZdoFileType.txt.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "}\nOPTIONAL {\n" +
                        //and also alto children with ocr text
                        "?alto <http://purl.org/dc/terms/isPartOf> ?page.\n" +
                        "?alto <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.binary.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "?alto <" + ZdoTerms.fileType.getURI() + "> \"" + ZdoFileType.alto.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                        "}\n}";
                QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
                ResultSet resultSet = queryExecution.execSelect();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.next();
                    Integer pageIndex = Integer.valueOf(querySolution.getLiteral("pageIndex").getString());
                    Resource userCopyResource = querySolution.getResource("userCopy");
                    if (userCopyResource != null) {
                        String userCopyUrl = userCopyResource.getURI();
                        if (userCopyUrl != null) {
                            if("video/mp4".equals(querySolution.getLiteral("ucMime").getString())) {
                                if(videoUrl != null) {
                                    logger.error("More than one video per document encountered. There can only be one.");
                                }
                                videoUrl = userCopyUrl;
                            }
                            else {
                                imageMap.put(pageIndex, userCopyUrl);
                            }
                        }
                    }
                    Resource thumbnailResource = querySolution.getResource("thumb");
                    if (thumbnailResource != null) {
                        String thumbUrl = thumbnailResource.getURI();
                        if (thumbUrl != null) {
                            thumbMap.put(pageIndex, thumbUrl);
                        }
                    }
                    Resource txtResource = querySolution.getResource("txt");
                    if (txtResource != null) {
                        String txtUrl = txtResource.getURI();
                        if (txtUrl != null) {
                            txtMap.put(pageIndex, txtUrl);
                        }
                    }
                    Resource altoResource = querySolution.getResource("alto");
                    if (altoResource != null) {
                        String altoUrl = altoResource.getURI();
                        if (altoUrl != null) {
                            altoMap.put(pageIndex, altoUrl);
                        }
                    }
                }

                if(videoUrl != null) {
                    inputDoc.addField("videoId", store.getOnlyIdFromUrl(videoUrl));
                }

                List<String> imageIds = new ArrayList<>();
                if (!imageMap.isEmpty()) {
                    for (String userCopyUrl : imageMap.values()) {
                        imageIds.add(store.getOnlyIdFromUrl(userCopyUrl));
                    }
                    inputDoc.addField("imageIds", imageIds);
                }

                if (!thumbMap.isEmpty()) {
                    List<String> thumbIds = new ArrayList<>();
                    for (String thumbUrl : thumbMap.values()) {
                        thumbIds.add(store.getOnlyIdFromUrl(thumbUrl));
                    }
                    inputDoc.addField("thumbIds", thumbIds);
                }

                List<String> txtIds = new ArrayList<>();
                if (!txtMap.isEmpty()) {
                    String fulltext = "";
                    for (String txtUrl : txtMap.values()) {
                        txtIds.add(store.getOnlyIdFromUrl(txtUrl));
                        InputStream in = new URL(txtUrl).openStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(in, writer, "utf-8");
                        String text = writer.toString();
                        fulltext += text + " ";
                    }
                    inputDoc.addField("fullText", fulltext.trim());
                }

                List<String> altoIds = new ArrayList<>();
                if(!altoMap.isEmpty()) {
                    for (String altoUrl : altoMap.values()) {
                        altoIds.add(store.getOnlyIdFromUrl(altoUrl));
                    }
                }

                ZdoModel kdrObject = store.get(model.get(ZdoTerms.kdrObject));
                String origPdfUrl = kdrObject.get(ZdoTerms.pdfUrl);
                String origEpubUrl = kdrObject.get(ZdoTerms.epubUrl);
                ZdoModel patchModel = new ZdoModel();   //Used to add new pdf and epub data to Fedora
                patchModel.setUrl(model.get(ZdoTerms.kdrObject));
                if("true".equals(model.get(ZdoTerms.allowPdfExport)) && !imageIds.isEmpty()) {
                    if(origPdfUrl == null) {
                        String pdfId = UUID.randomUUID().toString();
                        patchModel.add(ZdoTerms.pdfUrl, store.createUrl(pdfId));
                        String orgId = model.get(ZdoTerms.organization);

                        String watermarkId = null;
                        if ("true".equals(model.get(ZdoTerms.watermark))) {
                            watermarkId = organizationSettingsAccess.fetchOrgWatermark(orgId);
                            if (watermarkId == null) {
                                watermarkId = portalSettingsAccess.fetchPortalSettings().getWatermarkId();
                            }
                        }

                        PdfCreatorDto pdfCreatorDto = new PdfCreatorDto(pdfId, imageIds, altoIds, watermarkId, model.get(ZdoTerms.watermarkPosition));
                        Response response = ClientBuilder.newClient().target(IP_ENDPOINT + "pdf").request().post(Entity.json(pdfCreatorDto));
                        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                            throw new RuntimeException("Failed to call pdf creator in image processing war.");
                        }
                        inputDoc.addField("pdfId", pdfId);
                    }
                    else {  //When reindexing, pdf already exists
                        inputDoc.addField("pdfId", store.getOnlyIdFromUrl(origPdfUrl));
                    }
                }
                if("true".equals(model.get(ZdoTerms.allowEpubExport)) && !txtIds.isEmpty()) {
                    if(origEpubUrl == null) {
                        String epubId = UUID.randomUUID().toString();
                        patchModel.add(ZdoTerms.epubUrl, store.createUrl(epubId));
                        epubCreator.createBook(epubId, model.get(DCTerms.title), model.get(DCTerms.creator), txtIds);
                        inputDoc.addField("epubId", epubId);
                    }
                    else {
                        inputDoc.addField("epubId", store.getOnlyIdFromUrl(origEpubUrl));
                    }
                }
                store.patchMetadata(patchModel);    //warning, this does not go to triplestore
            }
        }

        logger.debug("Executing update of: {}...", store.removeTransactionFromUrl(model.getUrl()));

        return inputDoc;
    }

    public void updateUri(String uri) throws IOException {
        update(reconstructTreeToIndex(uri));
    }

    public ModelTreeNode reconstructTreeToIndex(String uri) throws IOException {
        ZdoModel model = store.get(uri);
        ModelTreeNode modelTreeNode = new ModelTreeNode();
        modelTreeNode.setModel(model);

        if(!ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) {
            String queryString = "SELECT ?subject WHERE {" +
                    "?subject <" + DCTerms.isPartOf.getURI() + "> <" + uri + ">." +
                    sparqlTools.createDocumentStateCondition("published") +
                    " }";
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
            ResultSet resultSet = queryExecution.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();
                String childUri = querySolution.getResource("subject").getURI();
                modelTreeNode.getChildren().add(reconstructTreeToIndex(childUri));
            }
        }
        return modelTreeNode;
    }

    protected SolrInputDocument modelToSolrInputDoc(ZdoModel model) {
        logger.debug("Constructing new SolrInputDocument...");

        final Map<String, SolrInputField> fields = new HashMap<>();

        //Add all Dublin Core terms
        for(String property : DCTools.getDcTermList()) {
            SolrInputField field = new SolrInputField(property);
            List<String> values = model.getAll(new PropertyImpl("http://purl.org/dc/terms/" + property));
            if(values.isEmpty()) continue;
            //Skip fields that were not ticked to be published
            String visible = model.get(new PropertyImpl("http://purl.org/dc/terms/" + property + "_visibility"));
            if("false".equals(visible) || "0".equals(visible)) {    //0 should not occur any more
                continue;
            }
            if("isPartOf".equals(property)) {   //remove ip address from isPartOf
                values.set(0, store.getOnlyIdFromUrl(values.get(0)));
            }
            if("".equals(values.get(0))) {
                values.set(0, "unknown");
            }

            field.addValue(values, INDEX_TIME_BOOST);
            fields.put(property, field);

            //Suggester data
            if("title".equals(property) || "creator".equals(property)) {
                SolrInputDocument suggesterDoc = new SolrInputDocument();
                String suggestVal = values.get(0).trim();
                if(!suggestVal.isEmpty() && !suggestVal.equals("unknown")) {
                    suggesterDoc.addField("suggesterData", values.get(0).trim());
                    dataForSuggester.add(suggesterDoc);
                }
            }
        }

        //Add system fields
        SolrInputField field = new SolrInputField("id");
        field.addValue(store.getOnlyIdFromUrl(model.getUrl()), INDEX_TIME_BOOST);
        fields.put("id", field);

        addSolrFieldFromFedoraProperty("inventoryId", ZdoTerms.inventoryId, model, fields);

        addSolrFieldFromFedoraProperty("zdoType", ZdoTerms.zdoType, model, fields);
        addSolrFieldFromFedoraProperty("zdoGroup", ZdoTerms.group, model, fields);
        addSolrFieldFromFedoraProperty("orgIdmId", ZdoTerms.organization, model, fields);
        addSolrFieldFromFedoraProperty("allowContentPublicly", ZdoTerms.allowContentPublicly, model, fields);
        addSolrFieldFromFedoraProperty("allowPdfExport", ZdoTerms.allowPdfExport, model, fields);
        addSolrFieldFromFedoraProperty("allowEpubExport", ZdoTerms.allowEpubExport, model, fields);
        addSolrFieldFromFedoraProperty("watermark", ZdoTerms.watermark, model, fields);
        addSolrFieldFromFedoraProperty("watermarkPosition", ZdoTerms.watermarkPosition, model, fields);
        addSolrFieldFromFedoraProperty("imgThumb", ZdoTerms.imgThumb, model, fields);
        addSolrFieldFromFedoraProperty("imgNormal", ZdoTerms.imgNormal, model, fields);

        String publishFromStr = model.get(ZdoTerms.publishFrom);
        if(publishFromStr != null) {
           String publishFromUtc = ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(publishFromStr)), ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            addSolrField("publishFrom", publishFromUtc, fields);
        }
        String publishToStr = model.get(ZdoTerms.publishTo);
        if(publishToStr != null) {
            String publishToUtc = ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(publishToStr)), ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            addSolrField("publishTo", publishToUtc, fields);
        }

        String created = model.get(DCTerms.created);
        if(created != null) {
            AtomicInteger yearStart = new AtomicInteger();
            AtomicInteger yearEnd = new AtomicInteger();
            AtomicBoolean startValid = new AtomicBoolean();
            AtomicBoolean endValid = new AtomicBoolean();
            YearNormalizer.normalizeCreatedYear(created, yearStart, startValid, yearEnd, endValid);
            if (startValid.get()) {
                addSolrField("yearStart", yearStart.get(), fields);
            }
            else {
                logger.warn("Year could not be normalized for input string " + created);
            }
            if (endValid.get()) {
                addSolrField("yearEnd", yearEnd.get(), fields);
            }
        }

        String orgName = orgNameMapping.get(model.get(ZdoTerms.organization));
        if(orgName == null) {
            orgName = "Neznámá";
        }
        addSolrField("organization", orgName, fields);

        String documentTypeId = model.get(ZdoTerms.documentType);   //type and subtype names must be found for id
        String documentSubTypeId = model.get(ZdoTerms.documentSubType);
        if(documentTypeId != null) {
            addSolrField("documentType", documentTypeAccess.getTypeNameForId(Integer.valueOf(documentTypeId)), fields);
        }
        if(documentSubTypeId != null) {
            addSolrField("documentSubType", documentTypeAccess.getSubTypeNameForId(Integer.valueOf(documentSubTypeId)), fields);
        }

        //Add customFields
        int fieldIndex = 0; //we actually start from 1
        do {
            fieldIndex++;
            String fieldName = model.get(new PropertyImpl("http://inqool.cz/zdo/1.0/customField_" + fieldIndex + "_name"));
            if(fieldName == null) break;
            fieldName = "customField_" + fieldName;
            String visible = model.get(new PropertyImpl("http://inqool.cz/zdo/1.0/customField_" + fieldIndex + "_visibility"));
            if("false".equals(visible) || "0".equals(visible)) continue;
            List<String> fieldValues = model.getAll(new PropertyImpl("http://inqool.cz/zdo/1.0/customField_" + fieldIndex));
            if("".equals(fieldValues.get(0))) {
                fieldValues.set(0, "unknown");
            }
            SolrInputField customField = new SolrInputField(fieldName);
            customField.addValue(fieldValues, INDEX_TIME_BOOST );
            fields.put(fieldName, customField);
        } while(true);

        SolrInputDocument solrInputDocument = new SolrInputDocument(fields);
        return solrInputDocument;
    }

    private void addSolrFieldFromFedoraProperty(String fieldName, Property property, ZdoModel model, Map<String, SolrInputField> fields) {
        addSolrField(fieldName, model.get(property), fields);
    }

    private void addSolrField(String fieldName, Object value, Map<String, SolrInputField> fields) {
        if(value == null) return;
        SolrInputField zdoTypeField = new SolrInputField(fieldName);
        zdoTypeField.addValue(value, INDEX_TIME_BOOST );
        fields.put(fieldName, zdoTypeField);
    }

    public Future<UpdateResponse> remove(final String id) {
        logger.debug("Received request for removal of: {}", id);
        try {
            SolrServer server = new HttpSolrServer(SOLR_MAIN_ENDPOINT);
            //first delete children
            //not needed, if _root_ is indexed, children are deleted with parent?
            //or now it doesn't work again? weird
            final UpdateResponse resp0 = server.deleteByQuery("_root_:\"" + id + "\"");
            if (resp0.getStatus() == 0) {
                logger.debug("Remove request was successful for children of: {}", id);
            } else {
                logger.error("Remove request has error, code: {} for pid: {}", resp0.getStatus(), id);
                return new AsyncResult<>(resp0);
            }

            final UpdateResponse resp1 = server.deleteById(id);
            if (resp1.getStatus() == 0) {
                logger.debug("Remove request was successful for: {}", id);
            } else {
                logger.error("Remove request has error, code: {} for pid: {}", resp1.getStatus(), id);
            }

            //Also delete suggester data belonging to the document root
            SolrServer suggesterServer = new HttpSolrServer(SOLR_SUGGESTER_ENDPOINT);
            final UpdateResponse resp2 = suggesterServer.deleteByQuery("belongsTo:\"" + id + "\"");
            if (resp2.getStatus() == 0) {
                logger.debug("Remove request was successful for suggester data of: {}", id);
            } else {
                logger.error("Remove request for suggester data has error, code: {} for pid: {}", resp2.getStatus(), id);
            }
            return new AsyncResult<>(resp1);
        } catch (final SolrServerException | IOException e) {
            logger.error("Delete Exception: {}", e);
            throw new RuntimeException(e);
        }
    }

    private void fetchOrgNames() {
        List<ZdoOrganization> orgList = picketLinkAccess.listOrganizations();
        for(ZdoOrganization org : orgList) {
            String idmId = org.getName();
            orgNameMapping.put(idmId, picketLinkAccess.removePrispevkovaOrganizaceFromOrgName(org.getDisplayName()));
        }
    }
}
