package com.inqool.dcap.integration.desa2.loader;

import com.inqool.dcap.common.DocumentTypeAccess;
import com.inqool.dcap.config.CustomProjectStageHolder;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.desa2.parser.MarcXmlParser;
import com.inqool.dcap.integration.desa2.parser.MetsParser;
import com.inqool.dcap.integration.desa2.parser.MetsParserOld;
import com.inqool.dcap.integration.exception.FailedToLoadException;
import com.inqool.dcap.integration.exception.FailedToParseException;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import gov.loc.marc21.slim.CollectionType;
import gov.loc.mets.Mets;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads metadata and connected resources from Package. Resulting ZdoModels can hold references to InputStream created
 * through the Package. User is responsible for closing them when appropriate.
 *
 * @author Matus Zamborsky (inQool)
 *
 */
@ApplicationScoped
public class Loader {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private MetsParser metsParser;

    @Inject
    private MarcXmlParser marcXmlParser;

    @Inject
    private MetsParserOld metsParserOld;

    @Inject
    private OrgMappingAccess2 orgMappingAccess2;

    @Inject
    private ProjectStage projectStage;

    @Inject
    private DocumentTypeAccess documentTypeAccess;


    public List<ZdoModel> loadMetadata(File sourceFolder, ZipFile zipFile, final String invId, final String orgIco, final String orgShortcut) throws FailedToLoadException {
        try {
            ZipEntry inputEntry = null;

            //KK skips METS and loads directly marcxml
            if(sourceFolder.getName().contains("KK")) {
                Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
                while(zipEntries.hasMoreElements()) {
                    ZipEntry zipEntry = zipEntries.nextElement();
                    if(zipEntry.getName().equals("mets.xml") || !zipEntry.getName().endsWith(".xml")) {
                        continue;
                    }
                    inputEntry = zipEntry;
                    break;
                }
            }
            else {  //Otherwise load METS
                inputEntry = zipFile.getEntry("METS_" + invId + ".xml");
                if (inputEntry == null) { //There is no PSP type mets, try AIP one
                    inputEntry = zipFile.getEntry("mets.xml");
                }
            }

            if (inputEntry == null) {
                throw new FailedToLoadException("No suitable metadata file found in " + zipFile.getName());
            }

            Object result = unmarshalStream(zipFile.getInputStream(inputEntry));
            List<ZdoModel> resultList;
            try {
                if (result instanceof Mets) {
                    resultList = metsParser.parse((Mets) result, sourceFolder, invId, orgShortcut);
                } else if (result instanceof CollectionType) {
                    resultList = marcXmlParser.parse((CollectionType) result, orgShortcut);
                } else {
                    throw new FailedToParseException("Unsupported metadata type.");
                }
            }
            catch (Exception e) {
                throw new FailedToParseException(e);
            }
            for (ZdoModel model : resultList) {
                if (ZdoType.isAbovePageCategory(model.get(ZdoTerms.zdoType))) {
                    model.add(ZdoTerms.group, ZdoGroup.KDR.name());
                    model.add(ZdoTerms.lockCount, "0");
                    if(CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) {
                        model.replaceValueOfProperty(ZdoTerms.organization, orgShortcut);
                    }
                    else {
                        model.replaceValueOfProperty(ZdoTerms.organization, orgMappingAccess2.getOrgId(orgIco));
                    }
                    //In SCK, we directly fill document types
                    if(CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage) || CustomProjectStageHolder.DevelopmentKudlajz.equals(projectStage)) {
                        fillDocTypes(model);
                        if(ZdoType.isRootCategory(model.get(ZdoTerms.zdoType)) && model.get(ZdoTerms.documentType) == null) {
                            throw new FailedToParseException("DocumentType has not been determined.");
                        }
                    }
                }
            }
            return resultList;
        } catch(Exception e) {
            throw new FailedToLoadException(e);
        }
    }

    private Object unmarshalStream(final InputStream inputFileStream) throws IOException, JAXBException {
        String ctxPackages = "org.purl.dc.elements._1:" +
                "org.openarchives.oai._2_0.oai_dc:" +
                "gov.loc.mets:" +
                "gov.loc.mods.v3:" +
                "gov.loc.marc21.slim";
        if(ProjectStage.Production.equals(projectStage) || ProjectStage.Staging.equals(projectStage)) {
            ctxPackages += ":cz.i.sbirkovepredmety._1";
        }
        else if(CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage)) {
            ctxPackages += ":cz.i.sbirkovepredmety._2";
        }
        final JAXBContext context = JAXBContext.newInstance(ctxPackages);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        Object result = unmarshaller.unmarshal(inputFileStream);
        inputFileStream.close();
        if(result instanceof JAXBElement) {
            result = ((JAXBElement) result).getValue();
        }
        return result;
    }

    private void fillDocTypes(ZdoModel model) {
        String zdoType = model.get(ZdoTerms.zdoType);
        String docType;
        String docSubType;
        if(ZdoType.monograph.name().equals(zdoType)) {
            docType = "Knihy";
            docSubType = "Knihy";
        }
        else if(ZdoType.periodical.name().equals(zdoType)) {
            docType = "Písemnosti a tisky";
            docSubType = "Písemnosti a tisky";
        }
        else {
            return;
        }
        int docTypeId = documentTypeAccess.findIdForTypeName(docType);
        int docSubTypeId = documentTypeAccess.findIdForSubTypeName(docSubType, docTypeId);

        model.add(ZdoTerms.documentType, String.valueOf(docTypeId));
        model.add(ZdoTerms.documentSubType, String.valueOf(docSubTypeId));
    }
}
