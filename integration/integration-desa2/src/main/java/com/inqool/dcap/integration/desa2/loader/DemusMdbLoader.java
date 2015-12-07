package com.inqool.dcap.integration.desa2.loader;

import com.healthmarketscience.jackcess.*;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * Reads Bach XLS file
 * @author Lukas Jane (inQool)
 */
@RequestScoped
@Path("/demus/")
public class DemusMdbLoader {

    @Inject
    @ConfigProperty(name = "demus.data.dir")
    private String DATA_DIR;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    private OrgMappingAccess2 orgMappingAccess2;

    @Inject
    private SparqlTools sparqlTools;

    @Inject
    private ProjectStage projectStage;

    private static HashMap<String, Property> columnsToExtract = new HashMap<>();
    static {
        columnsToExtract.put("Titul_SX", DCTerms.title);
        columnsToExtract.put("Popis_SX", DCTerms.description);
        columnsToExtract.put("Material_SX", DCTerms.format);
        columnsToExtract.put("Technika_SX", DCTerms.format);
        columnsToExtract.put("Miry_SX", DCTerms.format);
        columnsToExtract.put("Datace_SX", DCTerms.issued);
        columnsToExtract.put("Pocet_SX", DCTerms.format);
        columnsToExtract.put("MistoVz_SX", DCTerms.spatial);
        columnsToExtract.put("Rada_SX", DCTerms.identifier);
        columnsToExtract.put("PorC_SX", DCTerms.identifier);
        columnsToExtract.put("Autor_SX", DCTerms.creator);
        columnsToExtract.put("Predmet_SX", DCTerms.format);
    }

    @Path("/load/")
    @GET
    public Response loadAvailableDemusData() {
        File mainFolder = new File(DATA_DIR);
        if(!mainFolder.exists()) {
            throw new RuntimeException("Main demus data input folder does not exist.");
        }
        FileFilter fileFilter = file -> (!file.isHidden() && file.isDirectory());
        File[] orgFolders = mainFolder.listFiles(fileFilter);
        if(orgFolders == null) {
            throw new RuntimeException("No organization folders found.");
        }
        for(File orgFolder : orgFolders) {
            String[] orgFolderParts = orgFolder.getName().split("_");
            if(orgFolderParts.length < 2) {
                logger.warn("Organization folder does not have expected name format - it must be \"ORGANIZAIONICO_ORGANIZATIONSHORCUT\": " + orgFolder.getName());
                continue;
            }
            String orgIco = orgFolderParts[0];
            String orgShortcut = orgFolderParts[1];
            if (!orgFolder.isDirectory()) {
                throw new RuntimeException("Organization file is not a folder.");
            }
            File[] insideOrgFiles = orgFolder.listFiles();
            for (File insideOrgFile : insideOrgFiles) {
                try {
                    String lowerCaseFileName = insideOrgFile.getName().toLowerCase();
                    if(!lowerCaseFileName.endsWith(".mdb")) {
                        logger.warn("Found non-mdb file, skipping.");
                        continue;
                    }
                    store.startTransaction();
                    demusToFedora(insideOrgFile, orgIco, orgShortcut);
                    store.commitTransaction();
                    if(projectStage.equals(ProjectStage.Production)) {
                        FileUtils.deleteDirectory(insideOrgFile);
                    }
                } catch (Exception e) {
                    logger.error("File could not be loaded.", e);
                    store.rollbackTransaction();
                }
            }
        }
        return Response.ok().build();
    }

    public void demusToFedora(final File file, String orgIco, String orgShortcut) throws IOException {
        List<Map<String, String>> propertyMapList = mdbToPropertyMapList(file);
        List<ZdoModel> modelList = propertyMapListToModelList(propertyMapList, orgIco, orgShortcut);
        saveModelToFedora(modelList);
    }

    private void saveModelToFedora(List<ZdoModel> modelList) throws IOException {
        for (ZdoModel model : modelList) {
            String invId = model.get(ZdoTerms.inventoryId);
            //If there already exists a record with same inventory id and group, delete it
            String queryString = "SELECT ?subject WHERE {\n" +
                    sparqlTools.createInventoryIdCondition(invId) +
                    "?subject <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.BACH.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                    " } LIMIT 1";
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
            ResultSet resultSet = queryExecution.execSelect();
            if(resultSet.hasNext()) {
                String originalUrl = resultSet.next().get("subject").asResource().getURI();
                store.delete(originalUrl);
            }

            model.setIndexable(true);
            store.update(model);
        }
    }

    private List<ZdoModel> propertyMapListToModelList(List<Map<String, String>> input, String orgIco, String orgShortcut) {
        List<ZdoModel> modelList = new ArrayList<>();

        String orgId = orgMappingAccess2.getOrgId(orgIco);
        if(orgId == null) {
            throw new RuntimeException("Organization ico not found in database.");
        }

        for(Map<String, String> item : input) {
            String uid = UUID.randomUUID().toString();
            String url = store.createUrl(uid);

            ZdoModel model = new ZdoModel(url, ZdoType.cho);
            model.add(ZdoTerms.group, ZdoGroup.DEMUS.name());
            model.add(ZdoTerms.organization, orgId);

            for(String key : columnsToExtract.keySet()) {
                String value = item.get(key);
                if(value == null) {
                    continue;
                }

                //Inventory id is made from two fields, treat it separately
                if("Rada_SX".equals(key)) {
                    String invId = item.get(key) + "-" + item.get("PorC_SX");
                    model.add(DCTerms.identifier, "invid:" + invId);
                    model.add(ZdoTerms.inventoryId, orgShortcut + "_" + invId.toUpperCase());
                }
                else if("PorC_SX".equals(key)) {    //Skip second part
                    continue;
                }
                else {  //Normal properties
                    model.add(columnsToExtract.get(key), value);
                }
            }
            modelList.add(model);
        }
        return modelList;
    }

    private List<Map<String, String>> mdbToPropertyMapList(final File file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        Database db = DatabaseBuilder.open(file);
        Table table = db.getTable("SbirkyX");
        for (Row row : table) {
            Map<String, String> record = new HashMap<>();
            for(String key : columnsToExtract.keySet()) {
                DataType columnType = table.getColumn(key).getType();
                if(DataType.INT.equals(columnType) || DataType.LONG.equals(columnType)) {
                    record.put(key, String.valueOf(row.getInt(key)));
                }
                else if(DataType.BYTE.equals(columnType)) {
                    record.put(key, String.valueOf(row.getByte(key)));
                }
                else {
                    record.put(key, row.getString(key));
                }
            }
            records.add(record);
        }
        return records;
    }
}
