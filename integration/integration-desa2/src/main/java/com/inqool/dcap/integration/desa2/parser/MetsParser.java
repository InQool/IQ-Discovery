/*
 * MetsParser2.java
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
package com.inqool.dcap.integration.desa2.parser;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.Lambda;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.exception.LambdaException;
import com.inqool.dcap.integration.desa2.GuardianAngel;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.jena.type.InvIdDataType;
import cz.i.sbirkovepredmety._1.SpecifickaMetadata;
import cz.i.sbirkovepredmety._2.TypMetadataOborova;
import gov.loc.mets.*;
import gov.loc.mods.v3.Mods;
import org.apache.xerces.dom.ElementNSImpl;
import org.openarchives.oai._2_0.oai_dc.Dc;
import org.w3c.dom.Element;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Parses Mets XML document and outputs list of RDF models.
 *
 * @author Matus Zamborsky (inQool)
 */
@RequestScoped
public class MetsParser {
    @Inject
    private DataStore store;

    @Inject
    private ModsParser modsParser;

    @Inject
    private DcParser dcParser;

    @Inject
    private ChoParser choParser;

    @Inject
    @Zdo
    private org.slf4j.Logger logger;

    @Inject
    private GuardianAngel guardianAngel;

    private int pageCounter;

    public List<ZdoModel> parse(final Mets mets, final File sourceFolder, final String invId, final String orgShortcut) {
        pageCounter = 0;
        List<Entry> logicalModels = parseModels(mets, "LOGICAL", sourceFolder, invId);
        List<Entry> physicalModels = parseModels(mets, "PHYSICAL", sourceFolder, invId);

        if(logicalModels.size() == 0 && physicalModels.size() == 0) {
            throw new RuntimeException("No model found in file.");
        }

        // replace parent of physical model if equal parent is found in logical model
        physicalModels = physicalModels.stream()
                .map(entry -> entry.setParent(entry.parent
                                .map(parent -> findEqualEntryOrSelf(parent, logicalModels))
                ))
                .collect(Collectors.toList());

        List<ZdoModel> intermediateList = Stream.concat(logicalModels.stream(), physicalModels.stream())
                .distinct() //fixme: potential file handle leak if models with open zipfile get discarded
                .map(this::updateMetadata)
                .map(this::updateParent)
                .map(entry -> updateInvId(entry, invId, orgShortcut))
                .map(entry -> entry.model)
                .collect(Collectors.toList());

        //Add possible CHO page and binary nodes from sbirky metadata
        if(logicalModels.size() >= 1) {
            if(logicalModels.get(0).predmet.isPresent()) {  //Zlin Cho is at the root
                if(logicalModels.size() > 1 || physicalModels.size() != 0) {
                    throw new RuntimeException("Cho should not contain more than 1 model from mets, check mets parser.");
                }
                intermediateList.addAll(choParser.parseFiles(logicalModels.get(0).predmet.get(), logicalModels.get(0).model, sourceFolder, invId));
            }
        }

        fixMonographies(intermediateList);
        return intermediateList;
    }

    //Fix monographies
    //Removes volumes and puts metadata and pages to monograph
    private void fixMonographies(List<ZdoModel> intermediateList) {
        String firstNodeType = intermediateList.get(0).get(ZdoTerms.zdoType);
        //Only fix monographs
        if(ZdoType.monograph.name().equals(firstNodeType)) {
            ZdoModel monograph = intermediateList.get(0);
            //Go through all nodes
            ListIterator<ZdoModel> listIterator = intermediateList.listIterator();
            listIterator.next(); //skip the monograph node
            while(listIterator.hasNext()) {
                ZdoModel model = listIterator.next();
                //If volume has metadata and monograph doesn't, we add them to monograph
                if(monograph.get(DCTerms.title) == null && ZdoType.volume.name().equals(model.get(ZdoTerms.zdoType))) {
                    StmtIterator stmtIterator = model.listStatements();
                    while(stmtIterator.hasNext()) {
                        Statement statement = stmtIterator.next();
                        Property property = statement.getPredicate();
                        if("http://purl.org/dc/terms/isPartOf".equals(property.getURI())) {
                            continue;
                        }
                        if(property.getURI().startsWith("http://purl.org/dc/terms/")) {
                            monograph.add(property, statement.getObject());
                        }
                    }
                }
                //Remove the volumes
                if(ZdoType.volume.name().equals(model.get(ZdoTerms.zdoType))) {
                    listIterator.remove();
                }
                //Put pages under monograph
                if(ZdoType.page.name().equals(model.get(ZdoTerms.zdoType))) {
                    model.replaceValueOfProperty(DCTerms.isPartOf, monograph.getUrl());
                }
            }
        }
    }

    private Entry updateInvId(final Entry entry, final String invId, final String orgShortcut) {
        String prefilledInvId = entry.model.get(ZdoTerms.inventoryId);
        if(prefilledInvId != null) {
            entry.model.replaceValueOfProperty(ZdoTerms.inventoryId, (orgShortcut + "_" + prefilledInvId).toUpperCase());
            return entry; //Inventory id already filled in
        }

        String type = entry.model.get(ZdoTerms.zdoType);

        if(ZdoType.binary.name().equals(type) || ZdoType.page.name().equals(type)) {  //dont set invId to binaries
            return entry;
        }

        String inventoryId = "defaultInventoryId";

        if(ZdoType.bornDigital.name().equals(type) || ZdoType.cho.name().equals(type)) {
            inventoryId = invId;
        }
        else {
            //Kapurka code
            String[] splited = invId.split("_");
            String x, y, z;
            switch (splited.length) {
                case 1:
                case 2:
                    inventoryId = splited[0];
                    break;
                case 3:
                case 4:
                case 5:
                    x = splited[0];
                    y = splited[1];
                    if (ZdoType.periodical.name().equals(type)) {
                        int dashPosition = x.indexOf("-");
                        if (dashPosition > 5) {
                            inventoryId = x.substring(0, dashPosition);
                        } else {
                            inventoryId = x;
                        }
                    } else if (ZdoType.volume.name().equals(type)) {
                        inventoryId = x + "_" + y;
                    } else if (ZdoType.issue.name().equals(type)) {
                        inventoryId = invId;
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid name of file detected in Kapurka function.");
            }
        }
        final String inventoryIdFin = inventoryId;

        return Optional.of(entry)
                .map(ent -> entry.model.add(entry.model.getSubject(), DCTerms.identifier, inventoryIdFin, InvIdDataType.get()))
                .map(ent -> entry.model.add(ZdoTerms.inventoryId, (orgShortcut + "_" + inventoryIdFin).toUpperCase()))
                .map(model -> entry)
                .get();
    }

    private Entry updateParent(final Entry entry) {
        return entry.parent
                .map(parent -> entry.model.add(DCTerms.isPartOf, parent.model.getUrl()))
                .map(model -> entry)
                .orElse(entry);
    }

    private Entry updateMetadata(final Entry entry) {
        if(entry.metadataOborova.isPresent()) {
            choParser.parseMetadata(entry.metadataOborova.get(), entry.model);
            if (entry.dc.isPresent()) {
                dcParser.parse(entry.dc.get(), entry.model);
            }
            if(entry.model.get(DCTerms.type) == null) {
                entry.model.add(DCTerms.type, "společenskovědní sbírky");
            }
        }
        if(entry.predmet.isPresent()) {
            choParser.parseMetadata(entry.predmet.get(), entry.model);
            if (entry.dc.isPresent()) {
                dcParser.parse(entry.dc.get(), entry.model);
            }
        }
        else {
            if (entry.mods.isPresent()) {
                modsParser.parse(entry.mods.get(), entry.model);
            } else {
                if (entry.dc.isPresent()) {
                    dcParser.parse(entry.dc.get(), entry.model);
                }
            }
        }
        return entry;
    }

    private Entry findEqualEntryOrSelf(final Entry entry, final List<Entry> entries) {
        return entries.stream()
                .filter(ent -> ent.equals(entry))
                .findFirst()
                .orElse(entry);
    }

    private List<Entry> parseModels(final Mets mets, final String type, final File sourceFolder, String invId) {
        return Lambda.stream(mets.getStructMaps())
                .filter(structMap -> type.equals(structMap.getTYPE()))
                .flatMap(structMap -> parseStructMap(structMap, sourceFolder, invId))
                .collect(Collectors.toList());
    }

    private Stream<Entry> parseStructMap(final StructMapType structMap, final File sourceFolder, String invId) {
        return parseDivRecursive(structMap.getDiv(), null, sourceFolder, invId);
    }

    private Stream<Entry> parseDivRecursive(final DivType div, final Entry parent, final File sourceFolder, String invId) {
        Optional<Entry> primalEntry = parseDiv(div, parent);    //parse metadata
        if(!primalEntry.isPresent()) {
            return Stream.empty();
        }
        Entry entry = primalEntry.get();
        if(entry.predmet.isPresent()) { //we dont continue to parse cho file structure here - it will be read from sbirky metadata
            return Stream.of(entry);
        }
        if(parent != null && parent.model.get(ZdoTerms.zdoType).equals(ZdoType.BORNDIGITAL)) {
            if(entry.model.get(ZdoTerms.zdoType).equals(ZdoType.CHO)) { //we leave this bad boy out
                return parseSubDivs(div, parent, sourceFolder, invId);
            }
            return parseMPtrs(div, parent, sourceFolder, invId);
        }
        //If there is another model under CHO, skip it and put its files directly under CHO
        if(parent != null && parent.model.get(ZdoTerms.zdoType).equals(ZdoType.CHO) && entry.model.get(ZdoTerms.zdoType).equals(ZdoType.BORNDIGITAL)) {
            return Lambda.concat(
                    parseFilePtrs(div, parent, sourceFolder, invId),
                    parseMPtrs(div, parent, sourceFolder, invId),
                    parseSubDivs(div, parent, sourceFolder, invId)
            );
        }

        return Lambda.concat(
                Stream.of(entry),
                parseFilePtrs(div, entry, sourceFolder, invId), //parse files associated with metadata
                /*parseMPtrs(div, entry, sourceFolder, invId),*/ //don't, we need file names and page numbers, and they are not in here, we have to get it from sbirky metadata
                parseSubDivs(div, entry, sourceFolder, invId)   //parse children
        );
    }

    private Stream<Entry> parseSubDivs(final DivType div, final Entry parent, final File sourceFolder, String invId) {
        return Lambda.stream(div.getDivs())
                .flatMap(child -> parseDivRecursive(child, parent, sourceFolder, invId));
    }

    private Optional<Entry> parseDiv(final DivType div, final Entry parent) {
        //Get div's metadata
        List<Object> metadataNodes = getMetadataNodes(div);
        Optional<Mods> mods = Optional.empty();
        Optional<Dc> dc = Optional.empty();
        Optional<SpecifickaMetadata> specifickaMetadata = Optional.empty();
        Optional<TypMetadataOborova> metadataOborova = Optional.empty();
        for (Object metadataNode : metadataNodes) {
            if(metadataNode instanceof Dc) {
                dc = Optional.of((Dc) metadataNode);
            }
            if(metadataNode instanceof Mods) {
                mods = Optional.of((Mods) metadataNode);
            }
            if(metadataNode instanceof SpecifickaMetadata) {
                specifickaMetadata = Optional.of((SpecifickaMetadata) metadataNode);
            }
            if(metadataNode instanceof TypMetadataOborova) {
                metadataOborova = Optional.of((TypMetadataOborova) metadataNode);
            }
        }

        Optional<ZdoType> typeOpt = findType(div.getTYPE());
        if(!typeOpt.isPresent()) {
            throw new RuntimeException("Unrecognized type found!");
        }

        ZdoType type = typeOpt.get();
        if(ZdoType.throwAway.equals(type)) {
            return Optional.empty();
        }

        if(type.equals(ZdoType.cho) && !specifickaMetadata.isPresent() && !metadataOborova.isPresent()) {    //file without sbirky metadata is actually not cho, but bornDigital
            type = ZdoType.bornDigital;
        }

        ZdoModel model;
        if(type.equals(ZdoType.spine)) {
            model = new ZdoModel(createURI(), ZdoType.page);
            model.add(ZdoTerms.cannotBeCoverPage, "true");  //we just need to somehow remember that this is just a book spine picture until we create book thumbnails from cover pages
        }
        else {
            model = new ZdoModel(createURI(), type);
        }
        fillOrderNumbers(model, div);
        return Optional.of(new Entry(model, mods, dc, specifickaMetadata, metadataOborova, getDivDmdId(div), Optional.ofNullable(parent)));
    }

    //Parsing MPTRs in CHOs linking to other zip files, it wasn't good enough - no page numberings
    private Stream<Entry> parseMPtrs(final DivType div, final Entry entry, final File sourceFolder, String invId){
        return Lambda.stream(div.getMptrs())
                .map(DivType.Mptr::getHref)
                .flatMap(fileHref -> parseExternalLocat(fileHref, entry, sourceFolder, invId));
    }

    private Stream<Entry> parseExternalLocat(String fileHref, Entry parent, File sourceFolder, String invId) {
        try {
            String filename = fileHref.replace("/", "_") + ".zip";
            File file = new File(sourceFolder, filename);
            if (!file.exists()) {
                throw new RuntimeException("File mentioned in mets not found: " + filename + " referenced from " + sourceFolder.getAbsolutePath() + " " + invId);
            }
            ZipFile zipFile = new ZipFile(file);
            guardianAngel.open();
            Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
            ZipEntry userCopy = null;
            ZipEntry masterCopy = null;    //if user copy is not found, use master copy
            ZipEntry other = null;
            while (entryEnumeration.hasMoreElements()) {
                ZipEntry zipEntry = entryEnumeration.nextElement();
                if (zipEntry.getName().startsWith("UC_")) {
                    userCopy = zipEntry;
                } else if (zipEntry.getName().startsWith("MC_")) {
                    masterCopy = zipEntry;
                } else if (zipEntry.getName().endsWith(".pdf") || zipEntry.getName().endsWith(".mp4")) {
                    other = zipEntry;
                }
            }
            if (userCopy == null) {
                userCopy = masterCopy;
            }
            if (userCopy == null) {
                userCopy = other;
            }
            if (userCopy == null) {
                throw new RuntimeException("Nothing useful found in pack! " + filename);
            }

            String fileContentType = findContentTypeInner(userCopy.getName());
            if (fileContentType == null) {
                throw new RuntimeException("Failed to determine content type! " + userCopy.getName() + " in file " + filename);
            }

            ZdoModel model = new ZdoModel(createURI(), zipFile.getInputStream(userCopy));
            model.setFileToClose(zipFile);
            model.add(ZdoTerms.fileType, ZdoFileType.userCopy.name());
            model.add(ZdoTerms.mimeType, fileContentType);

            if(parent.model.get(ZdoTerms.zdoType).equals(ZdoType.CHO)) {    //We need to add pages to SCK CHOs...too bad
                ZdoModel pageModel = new ZdoModel(createURI(), ZdoType.page);
                pageModel.add(ZdoTerms.pageIndex, String.valueOf(++pageCounter));
                Entry pageEntry = new Entry(pageModel, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(parent));
                return Lambda.concat(
                        Stream.of(pageEntry),
                        Stream.of(new Entry(model, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(pageEntry))));
            }
            return Stream.of(new Entry(model, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(parent)));

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LambdaException(ex);
        }
    }

    private Stream<Entry> parseFilePtrs(final DivType div, final Entry entry, final File sourceFolder, String invId){
        List<Entry> entryList = new ArrayList<>();
        for(DivType.Fptr divFptr : div.getFptrs()) {
            Object fileIdObj = divFptr.getFILEID();
            if(fileIdObj instanceof FileType) {
                FileType fileType = (FileType) fileIdObj;
                for(FileType.FLocat fLocat : fileType.getFLocats()) {
                    Optional<Entry> optEntry = parseLocat(fLocat, entry, sourceFolder, invId);
                    if(optEntry.isPresent()) {
                        entryList.add(optEntry.get());
                    }
                }
            }
        }
        return Lambda.stream(entryList);
    }

    private Optional<Entry> parseLocat(final FileType.FLocat fileLocat, final Entry parent, final File sourceFolder, final String invId) {
        return Optional.of(fileLocat)
                .filter(location -> findFileType(location).isPresent())
                .filter(location -> findContentType(location).isPresent())
                .flatMap(location -> createModelFromFile(location.getHref(), sourceFolder, invId))
                .map(model -> model.add(ZdoTerms.fileType, findFileType(fileLocat).get().name()))
                .map(model -> model.add(ZdoTerms.mimeType, findContentType(fileLocat).get()))
                .map(model -> new Entry(model, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(parent)));
    }

    private Optional<ZdoModel> createModelFromFile(String origPath, final File sourceFolder, String invId) {
        try {
            //Name of the file itself, without folder path
            String fileName = origPath.substring(origPath.lastIndexOf("/") + 1);
            //Determine in which zip pack the file will be - strip first name part and change extension to zip
            String ending = fileName.substring(fileName.lastIndexOf("_"), fileName.lastIndexOf(".")) + ".zip";
            String packName = invId + ending;

            File file = new File(sourceFolder, packName);
            if(!file.exists()) {
/*                logger.error("File mentioned in mets not found: " + packName + " referenced from " + sourceFolder.getAbsolutePath() + " " + invId);
                return Optional.empty();*/
                throw new RuntimeException("File mentioned in mets not found: " + packName + " referenced from " + sourceFolder.getAbsolutePath() + " " + invId);
            }
            ZipFile zipFile = new ZipFile(file);
            guardianAngel.open();
            ZdoModel model = new ZdoModel(createURI(), zipFile.getInputStream(zipFile.getEntry(fileName)));
            model.setFileToClose(zipFile);
            return Optional.of(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<ZdoFileType> findFileType(final FileType.FLocat locat) {
        return Optional.of(locat)
                .filter(file -> "url".equals(file.getLOCTYPE().toLowerCase()))
                .map(FileType.FLocat::getHref)
                .map(String::toLowerCase)
                .map(href -> {
                    if (href.contains("usercopy/") || href.contains(".pdf")) {
                        return ZdoFileType.userCopy;
                    } else if (href.contains("txt/")) {
                        return ZdoFileType.txt;
                    } else if (href.contains("alto/")) {
                        return ZdoFileType.alto;
                    } else {
                        return null;
                    }
                });
    }

    private Optional<String> findContentType(final FileType.FLocat locat) {
        return Optional.of(locat)
                .filter(file -> "url".equals(file.getLOCTYPE().toLowerCase()))
                .map(FileType.FLocat::getHref)
                .map(String::toLowerCase)
                .map(this::findContentTypeInner);
    }

    private String findContentTypeInner(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".jp2")) {
            return "image/jp2";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".tif") || filename.endsWith(".tiff")) {
            return "image/tiff";
        } else if (filename.endsWith(".txt")) {
            return "text/plain";
        } else if (filename.endsWith(".xml")) {
            return "text/xml";
        } else if (filename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.endsWith(".mp4")) {
            return "video/mp4";
        } else {
            return null;
        }
    }

    private ZdoModel fillOrderNumbers(ZdoModel model, DivType div) {
        return Optional.ofNullable(div.getORDER())
                .filter(order -> ZdoType.page.name().equals(model.get(ZdoTerms.zdoType)))
                .map(order -> model.add(ZdoTerms.pageIndex, String.valueOf(order)))
                .orElse(model);
    }

    //From div, return its DMDID (first if multiple)
    private Optional<String> getDivDmdId(final DivType div) {
        return Lambda.stream(div.getDMDIDS())
                .map(this::parseDmdId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MdSecType::getID)
                .findFirst();
    }

    private List<Object> getMetadataNodes(final DivType div) {
        List<Object> metadataNodes = Lambda.stream(div.getDMDIDS())
                .findFirst()
                .map(this::parseDmdId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MdSecType::getMdWrap)
                .map(MdSecType.MdWrap::getXmlData)
                .map(MdSecType.MdWrap.XmlData::getAnies)
                .orElse(Collections.emptyList());
        for (int i = 0; i < metadataNodes.size(); i++) {
            if(metadataNodes.get(i) instanceof JAXBElement) {   //Unpack JAXBElements
                metadataNodes.set(i, ((JAXBElement) metadataNodes.get(i)).getValue());
            }
            if(metadataNodes.get(i) instanceof ElementNSImpl) { //SpecifickaMetadata from SCK has an ANY type, this breaks unmarshalling so this additional step is needed to get its content unmarshalled
                Element element = ((ElementNSImpl) metadataNodes.get(i)).getFirstElementChild();
                try {
                    final JAXBContext context = JAXBContext.newInstance("cz.i.sbirkovepredmety._2");
                    final Unmarshaller unmarshaller = context.createUnmarshaller();
                    Object result = unmarshaller.unmarshal(element);
                    if (result instanceof JAXBElement) {
                        result = ((JAXBElement) result).getValue();
                    }
                    if (result instanceof TypMetadataOborova) {
                        metadataNodes.set(i, result);
                    }
                    else {
                        throw new RuntimeException("Element " + result.getClass().getName() + " has an unexpected type, expected is TypMetadataOborova.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail while unmarshalling element " + element.getLocalName(), e);
                }
            }
        }
        return metadataNodes;
    }

    private String createURI() {
        return store.createUrl(UUID.randomUUID().toString());
    }

    private Optional<MdSecType> parseDmdId(final Object object) {
        return Optional.ofNullable(object)
                .filter(obj -> obj instanceof MdSecType)
                .map(obj -> (MdSecType) obj);
    }

    private Optional<ZdoType> findType(final String type) {
        switch (type.toLowerCase()) {
            case "monograph":
                return Optional.of(ZdoType.monograph);
            case "volume":
                return Optional.of(ZdoType.volume);
            case "periodical_title":
            case "periodical":
                return Optional.of(ZdoType.periodical);
            case "periodical_volume":
                return Optional.of(ZdoType.volume);
            case "issue":
                return Optional.of(ZdoType.issue);
            case "record":
                return Optional.of(ZdoType.bornDigital);
            case "spine":
                return Optional.of(ZdoType.spine);
            case "frontcover":
            case "cover":
            case "frontendsheet":
            case "blank":
            case "tableofcontents":
            case "backendsheet":
            case "backcover":
            case "titlepage":
            case "normalpage":
            case "advertisement":
            case "listofillustrations":
            case "map":
            case "listofmaps":
            case "table":
            case "listoftables":
            case "index":
            case "flyleaf":
            case "jacket":
            case "frontjacket":
                return Optional.of(ZdoType.page);
            case "file":
                return Optional.of(ZdoType.cho);
            case "chapter":
            case "supplement":
            case "picture":
            case "article":
                return Optional.of(ZdoType.throwAway);
            default:
                logger.warn("Unrecognized page type: " + type);
                return Optional.empty();
        }
    }

    public static class Entry {
        private final ZdoModel model;
        private final Optional<Mods> mods;
        private final Optional<Dc> dc;
        private final Optional<SpecifickaMetadata> predmet;
        private final Optional<TypMetadataOborova> metadataOborova;
        private final Optional<String> dmdId;
        private Optional<Entry> parent;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            return !(!dmdId.isPresent() || !dmdId.equals(entry.dmdId));
        }

        @Override
        public int hashCode() {
            return dmdId.hashCode();
        }

        public Entry(ZdoModel model, Optional<Mods> mods, Optional<Dc> dc, Optional<SpecifickaMetadata> predmet, Optional<TypMetadataOborova> metadataOborova, Optional<String> dmdId, Optional<Entry> parent) {
            this.model = model;
            this.mods = mods;
            this.dc = dc;
            this.predmet = predmet;
            this.metadataOborova = metadataOborova;
            this.dmdId = dmdId;
            this.parent = parent;
        }

        public Entry setParent(Optional<Entry> parent) {
            this.parent = parent;
            return this;
        }
    }
}