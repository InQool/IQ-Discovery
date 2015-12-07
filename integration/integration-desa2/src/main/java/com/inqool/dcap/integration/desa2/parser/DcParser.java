/*
 * ModsParser.java
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
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.common.DocumentTypeAccess;
import com.inqool.dcap.config.CustomProjectStageHolder;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.openarchives.oai._2_0.oai_dc.Dc;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Parses Dublin Core XML document and update provided RDF model.
 *
 * @author Lukas Jane (inQool)
 */
@ApplicationScoped
public class DcParser {

    @Inject
    private ProjectStage projectStage;

    @Inject
    private DocumentTypeAccess documentTypeAccess;

    private Map<Integer, String> sbirkyTranslationTable;

    public DcParser() {
        sbirkyTranslationTable = new HashMap<>();
        sbirkyTranslationTable.put(1, "Geologie");
        sbirkyTranslationTable.put(2, "Petrografie");
        sbirkyTranslationTable.put(3, "Mineralogie");
        sbirkyTranslationTable.put(4, "Paleontologie");
        sbirkyTranslationTable.put(5, "Botanika");
        sbirkyTranslationTable.put(6, "Mykologie");
        sbirkyTranslationTable.put(7, "Entomologie");
        sbirkyTranslationTable.put(8, "Zoologie");
        sbirkyTranslationTable.put(9, "Archeologie");
        sbirkyTranslationTable.put(10, "Historie");
        sbirkyTranslationTable.put(11, "Etnografie");
        sbirkyTranslationTable.put(12, "Antropologie");
        sbirkyTranslationTable.put(13, "Numizmatika");
        sbirkyTranslationTable.put(14, "Militária");
        sbirkyTranslationTable.put(15, "Výtvarné umění");
        sbirkyTranslationTable.put(16, "Umělecká řemesla");
        sbirkyTranslationTable.put(17, "Uměleckoprůmyslové práce");
        sbirkyTranslationTable.put(18, "Knihy");
        sbirkyTranslationTable.put(19, "Písemnosti a tisky");
        sbirkyTranslationTable.put(20, "Negativy a diapozitivy");
        sbirkyTranslationTable.put(21, "Fotografie, filmy, videozáznamy a jiná média");
        sbirkyTranslationTable.put(22, "Přenesené historické stavby (\"skanzen\")");
        sbirkyTranslationTable.put(23, "Věda, technika a průmyslová výroba");
        sbirkyTranslationTable.put(24, "Další");
        sbirkyTranslationTable.put(25, "Jiné");
        sbirkyTranslationTable.put(99, "Knihy");
    }

    public ZdoModel parse(final Dc dc, final ZdoModel model) {
        for(JAXBElement<SimpleLiteral> jaxbElement : dc.getTitlesAndCreatorsAndSubjects()) {
            String property = jaxbElement.getName().getLocalPart();
            Property prop = new PropertyImpl(DCTerms.NS + property);
            if(model.get(prop) == null) {   //Only fill if not filled yet
                for(String value : jaxbElement.getValue().getContent()) {
                    if(value != null && !value.isEmpty()) {
                        //SCK
                        if((CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage) || CustomProjectStageHolder.DevelopmentKudlajz.equals(projectStage))) {
                            //Parse Sbirky CES value inside CHO's subject into a Document type
                            if("subject".equals(property) && ZdoType.cho.name().equals(model.get(ZdoTerms.zdoType))) {
                                try {
                                    Scanner scanner = new Scanner(value).useDelimiter("\\D+");
                                    int collectionNumber;
                                    if(!scanner.hasNextInt()) {
                                        collectionNumber = 25;
                                    }
                                    else {
                                        collectionNumber = scanner.nextInt();
                                    }
                                    String collectionName = sbirkyTranslationTable.getOrDefault(collectionNumber, "Jiné");
                                    int docTypeId = documentTypeAccess.findIdForTypeName(collectionName);
                                    int docSubTypeId = documentTypeAccess.findIdForSubTypeName(collectionName, docTypeId);
                                    model.add(ZdoTerms.documentType, String.valueOf(docTypeId));
                                    model.add(ZdoTerms.documentSubType, String.valueOf(docSubTypeId));
                                    continue;
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to determine CHO document type", e);
                                }
                            }
                            else if("type".equals(property)) {
                                if(!value.isEmpty()) {
                                    model.replaceValueOfProperty(new PropertyImpl(DCTerms.NS + property), value);
                                }
                            }
                            else {
                                model.add(new PropertyImpl(DCTerms.NS + property), value);
                            }
                        }
                        else {  //Non SCK
                            if (!"identifier".equals(property)) {
                                model.add(new PropertyImpl(DCTerms.NS + property), value);
                            } /*else {
                                model.replaceValueOfProperty(ZdoTerms.inventoryId, value.toUpperCase());
                                model.add(DCTerms.identifier, value);
                            }*/
                        }
                    }
                }
            }
        }
        return model;
    }
}
