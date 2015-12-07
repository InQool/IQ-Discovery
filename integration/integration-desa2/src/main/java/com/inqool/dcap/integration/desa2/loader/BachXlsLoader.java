package com.inqool.dcap.integration.desa2.loader;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
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
@Path("/bach/")
public class BachXlsLoader {

    @Inject
    @ConfigProperty(name = "bach.data.dir")
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

    @Path("/load/")
    @GET
    public Response loadAvailableBachData() {
        File mainFolder = new File(DATA_DIR);
        if(!mainFolder.exists()) {
            throw new RuntimeException("Main data input folder does not exist.");
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
                    if(!(lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".xlsx"))) {
                        logger.warn("Found non-xls file, skipping.");
                        continue;
                    }
                    logger.info("Loading file " + insideOrgFile);
                    List<Map<String, String>> propertyMapList = xlsToPropertyMapList(insideOrgFile);
                    List<ZdoModel> modelList = propertyMapListToModelList(propertyMapList, orgIco, orgShortcut);
                    store.startTransaction();
                    saveModelToFedora(modelList);
                    store.commitTransaction();
                    if(projectStage.equals(ProjectStage.Production)) {
                        insideOrgFile.delete();
                    }
                } catch (Exception e) {
                    logger.error("File could not be loaded.", e);
                    store.rollbackTransaction();
                }
            }
        }
        logger.info("Bach data loaded.");
        return Response.ok().build();
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
            model.add(ZdoTerms.group, ZdoGroup.BACH.name());
            model.add(ZdoTerms.organization, orgId);

            for(String key : item.keySet()) {
                String value = item.get(key);
                switch(key) {
                    case "Předmět":
                        model.add(DCTerms.title, value);
                        break;
                    case "Popis":
                        model.add(DCTerms.description, value);
                        break;
                    case "Materiál":
                        model.add(DCTerms.format, value);
                        break;
                    case "Rozměry":
                        model.add(DCTerms.format, value);
                        break;
                    case "Datování":
                        model.add(DCTerms.issued, value);
                        break;
                    case "Počet kusů":
                        model.add(DCTerms.format, value);
                        break;
                    case "Lokalita":
                        model.add(DCTerms.spatial, value);
                        break;
                    case "Inventární číslo":
                        model.add(DCTerms.identifier, "invid:" + value);
                        model.add(ZdoTerms.inventoryId, orgShortcut.toUpperCase() + "_" + value.toUpperCase());
                        break;
                    case "Autor":
                        model.add(DCTerms.creator, value);
                        break;
                    case "Místo vydání":
                        model.add(DCTerms.publisher, value);
                        break;
                }
            }
            modelList.add(model);
        }
        return modelList;
    }

    private List<Map<String, String>> xlsToPropertyMapList(final File file) throws IOException, InvalidFormatException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Map<String, String>> records = new ArrayList<>();
            //Read rows starting from 3rd
            for (Row row : sheet) {
                if(row.getRowNum() < 2) {
                    continue;
                }
                //Skip rows beginning with empty cell
                if(row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
                    continue;
                }
                Map<String, String> record = new HashMap<>();
                //Read cells from the row along with cells from the header row, so long as header cell is not empty
                Row firstRow = sheet.getRow(0);
                for (Cell cell : row) {
                    if(cell.getColumnIndex() < firstRow.getLastCellNum()) {
                        record.put(firstRow.getCell(cell.getColumnIndex()).getStringCellValue(), cell.getStringCellValue());
                    }
                }
                records.add(record);
            }
            return records;
        }
    }
}
