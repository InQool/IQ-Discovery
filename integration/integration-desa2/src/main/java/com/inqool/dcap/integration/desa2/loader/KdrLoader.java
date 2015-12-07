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

package com.inqool.dcap.integration.desa2.loader;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.common.KeyValueAccess;
import com.inqool.dcap.common.entity.KeyValue;
import com.inqool.dcap.config.CustomProjectStageHolder;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.desa2.GuardianAngel;
import com.inqool.dcap.integration.desa2.loader.thumbnailer.Thumbnailer;
import com.inqool.dcap.integration.exception.FailedToLoadException;
import com.inqool.dcap.integration.exception.FailedToParseException;
import com.inqool.dcap.integration.model.*;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipFile;

@ApplicationScoped
@Path("/kdr/")
public class KdrLoader {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private Loader loader;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    @ConfigProperty(name = "fedora.endpoint")
    private String FEDORA_ENDPOINT;

    @Inject
    @ConfigProperty(name = "office.endpoint")
    private String OFFICE_ENDPOINT;

    @Inject
    @ConfigProperty(name = "testdata.dir")
    private String TESTDATA_DIR;

    @Inject
    private Thumbnailer thumbnailer;

    @Inject
    private GuardianAngel guardianAngel;

    @Inject
    private ProjectStage projectStage;

    @Inject
    private KeyValueAccess keyValueAccess;

    private AtomicInteger kdrDocsRemaining = new AtomicInteger(0);

    //Holds names of documents that are being worked upon, thus being locked for other threads
    private ConcurrentSkipListSet<String> lockSet = new ConcurrentSkipListSet<>();

    @Path("/load/")
    @GET
    public String loadData() throws FailedToLoadException, FailedToParseException, IOException {
        kdrDocsRemaining.set(countRemainingFiles());
        keyValueAccess.store(KeyValue.kdrDocsToLoadRemaining, kdrDocsRemaining.toString());
        loadKdrData();
        return "KDR data loaded to Fedora.";
    }

    public void loadKdrData() throws IOException {
        File mainFolder = new File(TESTDATA_DIR);
        if(!mainFolder.exists() || !mainFolder.isDirectory()) {
            throw new RuntimeException("Main data input folder does not exist.");
        }

        FileFilter fileFilter = file -> (!file.isHidden() && file.isDirectory());
        File[] orgFolders = mainFolder.listFiles(fileFilter);

        if(orgFolders.length == 0) {
            throw new RuntimeException("No organization folders found.");
        }
        for(File orgFolder : orgFolders) {
            String[] orgFolderParts = orgFolder.getName().split("_");
            if(orgFolderParts.length < 2) {
                //Hack because they changed folders to contain just name in SCK
                if(orgFolderParts.length == 1 && (CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.StagingSCK.equals(projectStage))) {
                    orgFolderParts = ("x_" + orgFolder.getName()).split("_");
                }
                else {
                    logger.warn("Organization folder does not have expected name format - it must be \"ORGANIZAIONICO_ORGANIZATIONSHORCUT\": " + orgFolder.getName());
                    continue;
                }
            }
            String orgIco = orgFolderParts[0];
            String orgShortcut = orgFolderParts[1];
            if(!orgFolder.isDirectory()) {
                logger.warn("Found file inside kdr folder that should not be there: " + orgFolder.getName());
                continue;
            }

            File[] insideOrgFolders = orgFolder.listFiles(fileFilter);
            for(File insideOrgfolder : insideOrgFolders) {
                if(!insideOrgfolder.isDirectory()) {
                    logger.warn("Found file inside organization folder that should not be there: " + insideOrgfolder.getName());
                    continue;
                }

                FileFilter itemFilter;
                if(insideOrgfolder.getName().contains("SIP") || insideOrgfolder.getName().contains("PSP")|| insideOrgfolder.getName().contains("NK")) {
                    itemFilter = file -> !file.isHidden() && file.getName().endsWith("_MTDPSP.zip");

                }
                else if(insideOrgfolder.getName().contains("KK")) {
                    itemFilter = file -> !file.isHidden();
                }
                else {
                    itemFilter = file -> !file.isHidden() && file.getName().endsWith("_FILE.zip");
                }
                File[] itemFiles = insideOrgfolder.listFiles(itemFilter);
                for(File itemFile : itemFiles) {
                    try {
                        //Only one thread may work on each itemFile
                        boolean gainedLock = lockSet.add(itemFile.getName());
                        if (!gainedLock) {
                            logger.info("Could not get lock on " + itemFile.getName() + ", it's probably being parsed by other thread.");
                            continue;
                        }
                        String invIdNamePart = itemFile.getName().substring(0, itemFile.getName().lastIndexOf("_"));
                        try {
                            logger.info("Load next pack from KDR: " + orgShortcut + " " + itemFile.getName());
                            LocalDateTime start = LocalDateTime.now();
                            guardianAngel.setNumClosed(0);
                            guardianAngel.setNumOpened(0);

                            try (ZipFile zipFile = new ZipFile(itemFile.getPath())) {
                                List<ZdoModel> models = loader.loadMetadata(insideOrgfolder, zipFile, invIdNamePart, orgIco, orgShortcut);
                                store.startTransaction();
                                processFileModels(models);
                                store.commitTransaction();
                            }
                            if(projectStage.equals(ProjectStage.Production) || CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage)) {
                                try {   //Delete loaded files
                                    File[] thisItemFiles = insideOrgfolder.listFiles(file -> file.getName().startsWith(invIdNamePart + "_"));
                                    for (File fileOfThisItem : thisItemFiles) {
                                        if(!fileOfThisItem.delete()) {
                                            logger.error("Failed to delete file " + fileOfThisItem.getAbsolutePath() + ".");
                                        }
                                    }
                                } catch(Exception e) {
                                    logger.error("Exception while deleting items of " + invIdNamePart + ".", e);
                                }
                            }
                            keyValueAccess.store(KeyValue.kdrDocsToLoadRemaining, String.valueOf(kdrDocsRemaining.decrementAndGet()));
                            logger.info("Pack loaded, it took " + (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC)) + " s.");
                        } catch (Exception e) {
                            logger.error("Failed to load item " + orgShortcut + " " + itemFile.getName(), e);
                            store.rollbackTransaction();
                        }
                        guardianAngel.dump();
                    }
                    finally {
                        lockSet.remove(itemFile.getName());
                    }
                }
            }
        }
        logger.info("Kdr data loaded to Fedora.");

        if(CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) {
            logger.info("Calling autopublish on office server.");
            Response response = ClientBuilder
                    .newClient()
                    .target(OFFICE_ENDPOINT + "autopublish/start")
                    .request()
                    .post(null);
            if(!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                logger.error("Attempt to call autopublish returned " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
            }
        }
        logger.info("Kdr loader ending.");
    }

    private void processFileModels(List<ZdoModel> models) throws FailedToLoadException, IOException {
        logger.debug("Postprocessing file models.");
        //To parent nodes such as volume and periodical, only thumbnail of first page of issue is set
        AtomicBoolean thumbnailToParentsSet = new AtomicBoolean(false);

        /*
        If we find Periodical or Volume whose copy already is in Fedora, we dump it but we store the translation between their URLs to this map.
        Then, if we stumble upon their children, we use this mapping to change their isPartOf value to the one that already is in Fedora.
        Notice that this relies on parents being before children in the input list.
        Parents that are not in Fedora translate to themselves.
         */
        Map<String, String> parentToRealParentMap = new HashMap<>();
        Map<String, ZdoModel> modelHolder = new HashMap<>();

        boolean skipLoading = false;

        //Go through all nodes gained from one metadata file
        for (ZdoModel model : models) {
            if(!skipLoading) {
                //Don't save to fedora if a doc with the same inventory id is there already
                //Don't check for pages and binary content below
                if (ZdoType.isAbovePageCategory(model.get(ZdoTerms.zdoType))) {
                    String existingFedoraDocUrl;
                    String existingId = model.get(ZdoTerms.inventoryId);
                    if (existingId == null) {
                        throw new RuntimeException("Found document without any id.");
                    }
                    existingFedoraDocUrl = getInvIdIfAlreadyInStore(existingId);
                    if (existingFedoraDocUrl != null) {  //Node already in store, save URL translation to map and skip saving to Fedora
                        parentToRealParentMap.put(model.getUrl(), existingFedoraDocUrl);
                        ZdoModel existingModel = store.get(existingFedoraDocUrl);
                        if(projectStage.equals(ProjectStage.Production) || CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage)) {
                            updateOldModel(model, existingModel);
                        }
                        if (ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) { //If this node is duplicit, we dont even wanna store binary content, so we end loading this file immediately
                            if(projectStage.equals(ProjectStage.Production) || CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage)) {
                                deleteOffsprings(existingFedoraDocUrl);
                            }
                            else {
                                logger.error("this data already is loaded, skipping the rest.");
                                skipLoading = true;
                            }
                        }
                        continue;
                    }
                }

                //Node is not in store, map to itself, then insert to Fedora normally
                parentToRealParentMap.put(model.getUrl(), model.getUrl());

                //Maybe change our parent to one that already was in Fedora
                String parentUrl = model.get(DCTerms.isPartOf);
                if (parentUrl != null) {
                    if (!parentToRealParentMap.containsKey(parentUrl)) {
                        throw new RuntimeException("Parent was not parsed yet!!! Terrible terrible error!");    //this means nodes were not ordered in input list, and child came before parent
                    }
                    String newParentUrl = parentToRealParentMap.get(parentUrl);
                    if (!newParentUrl.equals(parentUrl)) {
                        model.replaceValueOfProperty(DCTerms.isPartOf, newParentUrl);
                    }
                }

                try {
                    //If this is usercopy
                    //Convert JPEG2000s to JPEG if needed
                    //And create and store thumbnail
                    if (ZdoType.binary.name().equals(model.get(ZdoTerms.zdoType))
                            && ZdoFileType.userCopy.name().equals(model.get(ZdoTerms.fileType))) {
                        thumbnailer.parseImageStream(model.getContent(), model.get(ZdoTerms.mimeType));

                        if (!"video/mp4".equals(model.get(ZdoTerms.mimeType))) {
                            File jpegImage = thumbnailer.getJpegImage();
                            model.setContent(new FileInputStream(jpegImage));
                            model.replaceValueOfProperty(ZdoTerms.mimeType, "image/jpeg");
                        }

                        File thumbnail = thumbnailer.getJpegThumbnail();
                        String thumbnailUuid = UUID.randomUUID().toString();

                        //Upload thumb to Fedora
                        FileInputStream fileInputStream = new FileInputStream(thumbnail);
                        ZdoModel thumbModel = new ZdoModel(store.createUrl(thumbnailUuid), fileInputStream);
                        thumbModel.add(ZdoTerms.fileType, ZdoFileType.thumb.name());
                        thumbModel.add(ZdoTerms.mimeType, "image/jpeg");
                        thumbModel.add(DCTerms.isPartOf, model.get(DCTerms.isPartOf));
                        store.update(thumbModel);

                        //Set as thumbnail for all ancestors
                        addThumbnailToAncestors(thumbnailUuid, model, thumbnailToParentsSet, modelHolder);
                    }

                    //Upload to Fedora
                    model.setIndexable(true);
                    modelHolder.put(model.getUrl(), model);

                } finally {
                    thumbnailer.clean();
                }
            }
            else {  //If the rest of the data is to be skipped, we still need to close the zipfiles
                makeSureZipFileIsClosed(model);
            }
        }
        for (ZdoModel model : modelHolder.values()) {
            store.update(model);
            makeSureZipFileIsClosed(model);
        }
    }

    private void updateOldModel(ZdoModel newModel, ZdoModel oldModel) throws IOException {
        Map<Property, List<RDFNode>> propertiesToUpdate = new HashMap<>();

        //Find all properties of the new model that we'll update
        StmtIterator iter = newModel.listStatements();
        while(iter.hasNext()) {
            Statement statement = iter.next();
            Property predicate = statement.getPredicate();
            if (DCTerms.NS.equals(predicate.getNameSpace())
                    || ZdoTerms.additionalMetadata.getURI().equals(predicate.getURI())
                    || ZdoTerms.documentType.getURI().equals(predicate.getURI())
                    || ZdoTerms.documentSubType.getURI().equals(predicate.getURI())) {    //Update only DC properties
                if (!propertiesToUpdate.containsKey(predicate)) {
                    propertiesToUpdate.put(predicate, new ArrayList<>());
                }
                propertiesToUpdate.get(predicate).add(statement.getObject());
            }
        }

        //Remove the properties from old model and add new ones
        for(Map.Entry<Property, List<RDFNode>> entry : propertiesToUpdate.entrySet()) {
            if(DCTerms.isPartOf.equals(entry.getKey())) {
                continue;   //these are our internal parent-child bonds, keep them
            }
            oldModel.removeAllValuesOfProperty(entry.getKey());
            oldModel.addAllRdfNodes(entry.getKey(), entry.getValue());
        }
        store.update(oldModel);
    }

    private void addThumbnailToAncestors(String thumbnailUuid, ZdoModel model, AtomicBoolean thumbnailToParentsSet, Map<String, ZdoModel> modelHolder) throws IOException {
        //If we recursed all the way to root, mark that parent thumbnails are set
        if(ZdoType.isRootCategory(model.get(ZdoTerms.zdoType))) {
            thumbnailToParentsSet.set(true);
        }

        //No more parents, end recursion
        String parentUrl = model.get(DCTerms.isPartOf);
        if(parentUrl == null) return;

        //Sometimes picture of a book spine comes first, but its page should bear this mark so we can skip it
        if("true".equals(model.get(ZdoTerms.cannotBeCoverPage))) {
            model.removeAllValuesOfProperty(ZdoTerms.cannotBeCoverPage);
            return;
        }

        //This is so damn complicated just because transactions and impossibility to update models in it
        ZdoModel parentModel = modelHolder.get(parentUrl);
        if(parentModel != null) {
            if(ZdoType.isAbovePageCategory(parentModel.get(ZdoTerms.zdoType)) && thumbnailToParentsSet.get()) {
                return;
            }
            parentModel.replaceValueOfProperty(ZdoTerms.imgThumb, thumbnailUuid);
        }
        else {
            parentModel = store.get(parentUrl);
            if(parentModel == null) throw new RuntimeException("Parent retrieval failed.");
            if(ZdoType.isAbovePageCategory(parentModel.get(ZdoTerms.zdoType)) && thumbnailToParentsSet.get()) {
                return;
            }
            parentModel.replaceValueOfProperty(ZdoTerms.imgThumb, thumbnailUuid);
            store.update(parentModel);
        }

        addThumbnailToAncestors(thumbnailUuid, parentModel, thumbnailToParentsSet, modelHolder);
    }

    private String getInvIdIfAlreadyInStore(String invId) {
        String queryString = "SELECT ?subject WHERE {\n" +
                "?subject <http://inqool.cz/zdo/1.0/inventoryId> \"" + invId + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                "?subject <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.KDR.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                " } LIMIT 1";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(resultSet.hasNext()) {
            return resultSet.next().get("subject").asResource().getURI();
        }
        return null;
    }

    private void makeSureZipFileIsClosed(ZdoModel model) throws IOException {
        ZipFile fileToClose = model.getFileToClose();
        if(fileToClose != null) {
            guardianAngel.close();
            fileToClose.close();
        }
    }

    private void deleteOffsprings(String modelUrl) {
        List<String> childrenUrls = findChildren(store.removeTransactionFromUrl(modelUrl));
        childrenUrls.forEach(this::deleteWithOffsprings);
    }

    private void deleteWithOffsprings(String modelUrl) {
        List<String> childrenUrls = findChildren(store.removeTransactionFromUrl(modelUrl));
        childrenUrls.forEach(this::deleteWithOffsprings);
        store.delete(modelUrl);
    }

    public List<String> findChildren(String url) {
        List<String> modelUrls = new ArrayList<>();
        String query = "SELECT ?subject WHERE {\n" +
                "  ?subject <" + DCTerms.isPartOf.getURI() + "> <" + url + ">.\n" +
                "}\n";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        ResultSet rs = queryExecution.execSelect();
        while (rs.hasNext()) {
            QuerySolution querySolution = rs.next();
            modelUrls.add(querySolution.getResource("subject").getURI());
        }
        return modelUrls;
    }

    private int countRemainingFiles() {
        int itemCount = 0;
        File mainFolder = new File(TESTDATA_DIR);
        if(!mainFolder.exists() || !mainFolder.isDirectory()) {
            throw new RuntimeException("Main data input folder does not exist.");
        }

        FileFilter fileFilter = file -> (!file.isHidden() && file.isDirectory());

        File[] orgFolders = mainFolder.listFiles(fileFilter);
        for(File orgFolder : orgFolders) {
            File[] insideOrgFolders = orgFolder.listFiles(fileFilter);
            for(File insideOrgfolder : insideOrgFolders) {
                FileFilter itemFilter = file -> !file.isHidden() && file.getName().endsWith("_FILE.zip");
                File[] itemFiles = insideOrgfolder.listFiles(itemFilter);
                for(File itemFile : itemFiles) {
                    itemCount++;
                }
            }
        }
        return itemCount;
    }
}
