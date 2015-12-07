/*
 * ChoParser.java
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
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.common.dto.AdditionalMetadata;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.desa2.GuardianAngel;
import com.inqool.dcap.integration.model.*;
import com.inqool.dcap.integration.service.DataStore;
import cz.i.sbirkovepredmety._1.SpecifickaMetadata;
import cz.i.sbirkovepredmety._2.*;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ApplicationScoped
public class ChoParser {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    private GuardianAngel guardianAngel;

    public void parseMetadata(SpecifickaMetadata specifickaMetadata, final ZdoModel model) {
        AdditionalMetadata additionalMetadata = AdditionalMetadata.ofModel(model);

        for (SpecifickaMetadata.Kapitola.Zaznam.Pole pole : specifickaMetadata.getKapitola().getZaznam().getPole()) {
            String value = pole.getValue();
            if(value == null || value.trim().isEmpty()) {
                continue;
            }
            switch(pole.getNazev()) {
                case "autor":
                    model.add(DCTerms.creator, value);
                    break;
                case "datum":
                    model.add(DCTerms.created, value);
                    break;
/*                case "skup":
                    model.add(DCTerms.subject, value);
                    break;
                case "pskup":
                    model.add(DCTerms.subject, value);
                    break;*/
                case "popis":
                    model.add(DCTerms.description, value);
                    break;
                case "titul":
                    model.add(DCTerms.title, value);
                    break;
                case "lokalita":
                    model.add(DCTerms.spatial, value);
                    break;
/*                case "katastr":
                    model.add(DCTerms.spatial, value);
                    break;*/
                default:
                    additionalMetadata.addIfNotEmpty(pole.getNazev(), value);
                    break;
            }
        }

        additionalMetadata.fitToModel(model);
    }

    public void parseMetadata(TypMetadataOborova metadataPredmetu, final ZdoModel model) {
        AdditionalMetadata additionalMetadata = AdditionalMetadata.ofModel(model);

        for (Object obj : metadataPredmetu.getPodrobnaMetadataOrPublikaceOrOborovaMetadata()) {
            //In fact element PodrobnaMetadata
            if(obj instanceof TypMetadataPredmetu) {
                TypMetadataPredmetu podrobnaMetadata = ((TypMetadataPredmetu) obj);

                additionalMetadata.addIfNotEmpty("predmet", podrobnaMetadata.getPredmet());
                additionalMetadata.addIfNotEmpty("popis", podrobnaMetadata.getPopis());
                additionalMetadata.addIfNotEmpty("popisLaicky", podrobnaMetadata.getPopisLaicky());

                if(podrobnaMetadata.getTechnickeParametry() != null) {
                    additionalMetadata.addIfNotEmpty("format", podrobnaMetadata.getTechnickeParametry().getFormat());
                    additionalMetadata.addIfNotEmpty("material", podrobnaMetadata.getTechnickeParametry().getMaterial());
                    additionalMetadata.addIfNotEmpty("rozmery", podrobnaMetadata.getTechnickeParametry().getRozmery());
                    additionalMetadata.addIfNotEmpty("technika", podrobnaMetadata.getTechnickeParametry().getTechnika());
                }
                if(podrobnaMetadata.getAlternativniCisla() != null) {
                    additionalMetadata.addIfNotEmpty("inventarniCislo", podrobnaMetadata.getAlternativniCisla().getInventarniCislo());
                    additionalMetadata.addIfNotEmpty("pomocneCislo", podrobnaMetadata.getAlternativniCisla().getPomocneCislo());
                    additionalMetadata.addIfNotEmpty("prirustkoveCislo", podrobnaMetadata.getAlternativniCisla().getPrirustkoveCislo());
                    additionalMetadata.addIfNotEmpty("signatura", podrobnaMetadata.getAlternativniCisla().getSignatura());
                }

                additionalMetadata.addIfNotEmpty("pocetKusu", podrobnaMetadata.getPocetKusu());
                additionalMetadata.addIfNotEmpty("publikovat", podrobnaMetadata.getPublikovat());
                String publikovat = podrobnaMetadata.getPublikovat();
                if(publikovat != null && !publikovat.isEmpty()) {
                    Scanner scanner = new Scanner(publikovat).useDelimiter("\\D+");
                    if(scanner.hasNextInt()) {
                        int res = scanner.nextInt();
                        switch (res) {
                            case 1:
                                model.add(ZdoTerms.publishHint, PublishHint.dontPublish.name());
                                break;
                            case 2:
                                model.add(ZdoTerms.publishHint, PublishHint.metadataOnly.name());
                                break;
                            case 3:
                                model.add(ZdoTerms.publishHint, PublishHint.displayOnly.name());
                                break;
                            case 4:
                                model.add(ZdoTerms.publishHint, PublishHint.allowDownload.name());
                                break;
                            default:
                                logger.warn("Unrecognized contents of element publikovat found: " + publikovat);
                        }
                    }
                }

                additionalMetadata.addIfNotEmpty("popisLaicky", podrobnaMetadata.getPopisLaicky());
                additionalMetadata.addIfNotEmpty("poznamka", podrobnaMetadata.getPoznamka());

                additionalMetadata.addIfNotEmpty("nazevSbirky", podrobnaMetadata.getNazevSbirky());
                additionalMetadata.addIfNotEmpty("nazevOrganizace", podrobnaMetadata.getNazevOrganizace());

                //Puvod
                TypUdalost puvod = podrobnaMetadata.getPuvod();
                if(puvod != null) {
                    if(puvod.getCas() != null) {
                        additionalMetadata.addIfNotEmpty("puvodObdobiUdalosti", puvod.getCas().getObdobiUdalosti());
                        if(puvod.getCas().getCasUdalosti() != null) {
                            additionalMetadata.addIfNotEmpty("puvodCasUdalosti", puvod.getCas().getCasUdalosti().toString());
                        }
                    }
                    if(puvod.getMisto() != null) {
                        additionalMetadata.addIfNotEmpty("puvodZeme", puvod.getMisto().getZeme());
                        additionalMetadata.addIfNotEmpty("puvodStat", puvod.getMisto().getStat());
                        additionalMetadata.addIfNotEmpty("puvodKatastralniUzemi", puvod.getMisto().getKatastralniUzemi());
                        additionalMetadata.addIfNotEmpty("puvodLokalita", puvod.getMisto().getLokalita());
                        additionalMetadata.addIfNotEmpty("puvodMapovyCtverec", puvod.getMisto().getMapovyCtverec());
                        if(puvod.getMisto().getSpecifickaLokalita() != null) {
                            additionalMetadata.addIfNotEmpty("puvodMincovna", puvod.getMisto().getSpecifickaLokalita().getMincovna());
                        }
                    }
                    if(puvod.getOsoba() != null) {
                        for (TypOsoba typOsoba : puvod.getOsoba()) {
                            additionalMetadata.addIfNotEmpty("puvodOsoba", typOsoba.getCeleJmeno());
                        }
                    }
                }
                //Nalez
                TypUdalost nalez = podrobnaMetadata.getNalez();
                if(nalez != null) {
                    if(nalez.getCas() != null) {
                        additionalMetadata.addIfNotEmpty("nalezObdobiUdalosti", nalez.getCas().getObdobiUdalosti());
                        if(nalez.getCas().getCasUdalosti() != null) {
                            additionalMetadata.addIfNotEmpty("nalezCasUdalosti", nalez.getCas().getCasUdalosti().toString());
                        }
                    }
                    if(nalez.getMisto() != null) {
                        additionalMetadata.addIfNotEmpty("nalezZeme", nalez.getMisto().getZeme());
                        additionalMetadata.addIfNotEmpty("nalezStat", nalez.getMisto().getStat());
                        additionalMetadata.addIfNotEmpty("nalezKatastralniUzemi", nalez.getMisto().getKatastralniUzemi());
                        additionalMetadata.addIfNotEmpty("nalezLokalita", nalez.getMisto().getLokalita());
                        additionalMetadata.addIfNotEmpty("nalezMapovyCtverec", nalez.getMisto().getMapovyCtverec());
                        if(nalez.getMisto().getSpecifickaLokalita() != null) {
                            additionalMetadata.addIfNotEmpty("nalezMincovna", nalez.getMisto().getSpecifickaLokalita().getMincovna());
                        }
                    }

                    if(nalez.getOsoba() != null) {
                        for (TypOsoba typOsoba : nalez.getOsoba()) {
                            additionalMetadata.addIfNotEmpty("nalezOsoba", typOsoba.getCeleJmeno());
                        }
                    }
                }
            }
            if(obj instanceof TypMetadataPublikace) {
                TypMetadataPublikace metadataPublikace = ((TypMetadataPublikace) obj);

                //Creators
                if(metadataPublikace.getAutori() != null && metadataPublikace.getAutori().getAutorOrEditorOrPrekladatel() != null) {
                    for (JAXBElement<TypOsoba> typOsobaJAXBElement : metadataPublikace.getAutori().getAutorOrEditorOrPrekladatel()) {
                        TypOsoba osoba = typOsobaJAXBElement.getValue();
                        String localName = typOsobaJAXBElement.getName().getLocalPart();
                        additionalMetadata.addIfNotEmpty(localName, osoba.getCeleJmeno());
                    }
                }

                //Issued
                TypVydaniPublikace typVydaniPublikace = metadataPublikace.getVydani();
                if(typVydaniPublikace != null) {
                    additionalMetadata.addIfNotEmpty("isbn", typVydaniPublikace.getISBN());
                    additionalMetadata.addIfNotEmpty("issn", typVydaniPublikace.getISSN());
                    additionalMetadata.addIfNotEmpty("nakladatel", typVydaniPublikace.getNakladatel());
                    if(typVydaniPublikace.getRokVydani() != null) {
                        additionalMetadata.addIfNotEmpty("rokVydani", typVydaniPublikace.getRokVydani().toString());
                    }
                    additionalMetadata.addIfNotEmpty("mistoVydani", typVydaniPublikace.getMistoVydani());

                    additionalMetadata.addIfNotEmpty("edice", typVydaniPublikace.getEdice());
                    additionalMetadata.addIfNotEmpty("oznaceniVydani", typVydaniPublikace.getOznaceniVydani());
                    additionalMetadata.addIfNotEmpty("pocetVytisku", typVydaniPublikace.getPocetVytisku());
                    additionalMetadata.addIfNotEmpty("rozsah", typVydaniPublikace.getRozsah());
                    additionalMetadata.addIfNotEmpty("vytiskl", typVydaniPublikace.getVytiskl());
                }

                //Other
                TypUdajePublikace udajePublikace = metadataPublikace.getKnihovniUdaje();
                if(udajePublikace != null) {
                    additionalMetadata.addIfNotEmpty("jazykDokumentu", udajePublikace.getJazykDokumentu());
                    additionalMetadata.addIfNotEmpty("nazev", udajePublikace.getNazev());
                    additionalMetadata.addIfNotEmpty("podnazev", udajePublikace.getPodnazev());

                    additionalMetadata.addIfNotEmpty("druhDokumentu", udajePublikace.getDruhDokumentu());
                    additionalMetadata.addIfNotEmpty("signatura", udajePublikace.getSignatura());
                    additionalMetadata.addIfNotEmpty("fond", udajePublikace.getFond());
                    additionalMetadata.addIfNotEmpty("jmenneOdkazy", udajePublikace.getJmenneOdkazy());
                    additionalMetadata.addIfNotEmpty("vecneOdkazy", udajePublikace.getVecneOdkazy());
                    additionalMetadata.addIfNotEmpty("knihopisnaPoznamka", udajePublikace.getKnihopisnaPoznamka());
                    additionalMetadata.addIfNotEmpty("predmetovaHesla", udajePublikace.getPredmetovaHesla());
                    additionalMetadata.addIfNotEmpty("rozpisSvazku", udajePublikace.getRozpisSvazku());
                    additionalMetadata.addIfNotEmpty("udajeOPeriodiku", udajePublikace.getUdajeOPeriodiku());
                }
            }
            if(obj instanceof TypMetadataOborova.OborovaMetadata) {
                List<Object> prirodovednaOrNumismaticka = ((TypMetadataOborova.OborovaMetadata) obj).getPrirodovednaOrNumismaticka();
                if(prirodovednaOrNumismaticka != null) {
                    for (Object o : prirodovednaOrNumismaticka) {
                        if (o instanceof TypMetadataPrirodovedna) {
                            TypMetadataPrirodovedna metadataPrirodovedna = (TypMetadataPrirodovedna) o;
                            TypTaxonomickaKategorie taxonomickaKategorie = metadataPrirodovedna.getTaxonomickaKategorie();
                            if(taxonomickaKategorie != null) {
                                additionalMetadata.addIfNotEmpty("rise", taxonomickaKategorie.getRise());
                                additionalMetadata.addIfNotEmpty("kmen", taxonomickaKategorie.getKmen());
                                additionalMetadata.addIfNotEmpty("oddeleni", taxonomickaKategorie.getOddeleni());
                                additionalMetadata.addIfNotEmpty("trida", taxonomickaKategorie.getTrida());
                                additionalMetadata.addIfNotEmpty("rad", taxonomickaKategorie.getRad());
                                additionalMetadata.addIfNotEmpty("celed", taxonomickaKategorie.getCeled());
                                additionalMetadata.addIfNotEmpty("rod", taxonomickaKategorie.getRod());
                                additionalMetadata.addIfNotEmpty("druh", taxonomickaKategorie.getDruh());
                            }
                        } else if (o instanceof TypMetadataNumismaticka) {
                            TypMetadataNumismaticka metadataNumismaticka = (TypMetadataNumismaticka) o;
                            TypNumismatickePopisy numismatickePopisy = metadataNumismaticka.getNumismatickePopisy();
                            if(numismatickePopisy != null) {
                                additionalMetadata.addIfNotEmpty("avers", numismatickePopisy.getAvers());
                                additionalMetadata.addIfNotEmpty("hrana", numismatickePopisy.getHrana());
                                additionalMetadata.addIfNotEmpty("revers", numismatickePopisy.getRevers());
                            }
                        }
                    }
                }
            }
        }
        additionalMetadata.fitToModel(model);
    }

/*    public void parseMetadata(TypMetadataOborova metadataPredmetu, final ZdoModel model) {
        AdditionalMetadata additionalMetadata = AdditionalMetadata.ofModel(model);

        for (Object obj : metadataPredmetu.getPodrobnaMetadataOrPublikaceOrOborovaMetadata()) {
            if(obj instanceof TypMetadataPredmetu) {    //In fact element PodrobnaMetadata
                TypMetadataPredmetu podrobnaMetadata = ((TypMetadataPredmetu) obj);

                addIfNotEmpty(model, DCTerms.title, podrobnaMetadata.getPredmet());
                addIfNotEmpty(model, DCTerms.description, podrobnaMetadata.getPopis());

                if(podrobnaMetadata.getTechnickeParametry() != null) {
                    addIfNotEmpty(model, DCTerms.format, podrobnaMetadata.getTechnickeParametry().getFormat());
                    addIfNotEmpty(model, DCTerms.format, podrobnaMetadata.getTechnickeParametry().getMaterial());
                    addIfNotEmpty(model, DCTerms.format, podrobnaMetadata.getTechnickeParametry().getRozmery());
                    addIfNotEmpty(model, DCTerms.format, podrobnaMetadata.getTechnickeParametry().getTechnika());
                }
                if(podrobnaMetadata.getAlternativniCisla() != null) {
                    additionalMetadata.addIfNotEmpty("Původní inventární číslo", podrobnaMetadata.getAlternativniCisla().getInventarniCislo());
                    additionalMetadata.addIfNotEmpty("Pomocné číslo", podrobnaMetadata.getAlternativniCisla().getPomocneCislo());
                    additionalMetadata.addIfNotEmpty("Přírůstkové číslo", podrobnaMetadata.getAlternativniCisla().getPrirustkoveCislo());
                    additionalMetadata.addIfNotEmpty("Signatura", podrobnaMetadata.getAlternativniCisla().getSignatura());
                }

                additionalMetadata.addIfNotEmpty("Počet kusů", podrobnaMetadata.getPocetKusu());
                additionalMetadata.addIfNotEmpty("Publikovat", podrobnaMetadata.getPublikovat());

                additionalMetadata.addIfNotEmpty("Popis laicky", podrobnaMetadata.getPopisLaicky());
                additionalMetadata.addIfNotEmpty("Poznámka", podrobnaMetadata.getPoznamka());

                additionalMetadata.addIfNotEmpty("Název sbírky", podrobnaMetadata.getNazevSbirky());

                //Puvod
                TypUdalost puvod = podrobnaMetadata.getPuvod();
                if(puvod != null) {
                    if(puvod.getCas() != null) {
                        addIfNotEmpty(model, DCTerms.temporal, puvod.getCas().getObdobiUdalosti());
                        if(puvod.getCas().getCasUdalosti() != null) {
                            model.add(DCTerms.created, String.valueOf(puvod.getCas().getCasUdalosti().toGregorianCalendar().toZonedDateTime().getYear()));
                        }
                    }
                    if(puvod.getMisto() != null) {
                        addIfNotEmpty(model, DCTerms.spatial, puvod.getMisto().getZeme());
                        addIfNotEmpty(model, DCTerms.spatial, puvod.getMisto().getStat());
                        addIfNotEmpty(model, DCTerms.spatial, puvod.getMisto().getKatastralniUzemi());
                        addIfNotEmpty(model, DCTerms.spatial, puvod.getMisto().getLokalita());
                        addIfNotEmpty(model, DCTerms.spatial, puvod.getMisto().getMapovyCtverec());
                        if(puvod.getMisto().getSpecifickaLokalita() != null) {
                            addIfNotEmpty(model, DCTerms.spatial, puvod.getMisto().getSpecifickaLokalita().getMincovna());
                        }
                    }
                    if(puvod.getOsoba() != null) {
                        for (TypOsoba typOsoba : puvod.getOsoba()) {
                            model.add(DCTerms.creator, typOsoba.getCeleJmeno());
                        }
                    }
                }
                //Nalez
                TypUdalost nalez = podrobnaMetadata.getNalez();
                if(nalez != null) {
                    if(nalez.getCas() != null) {
                        model.add(DCTerms.temporal, nalez.getCas().getObdobiUdalosti());
                        if(nalez.getCas().getCasUdalosti() != null) {
                            model.add(DCTerms.created, String.valueOf(nalez.getCas().getCasUdalosti().getYear()));
                        }
                    }
                    if(nalez.getMisto() != null) {
                        addIfNotEmpty(model, DCTerms.spatial, nalez.getMisto().getZeme());
                        addIfNotEmpty(model, DCTerms.spatial, nalez.getMisto().getStat());
                        addIfNotEmpty(model, DCTerms.spatial, nalez.getMisto().getKatastralniUzemi());
                        addIfNotEmpty(model, DCTerms.spatial, nalez.getMisto().getLokalita());
                        addIfNotEmpty(model, DCTerms.spatial, nalez.getMisto().getMapovyCtverec());
                        if(nalez.getMisto().getSpecifickaLokalita() != null) {
                            addIfNotEmpty(model, DCTerms.spatial, nalez.getMisto().getSpecifickaLokalita().getMincovna());
                        }
                    }

                    if(nalez.getOsoba() != null) {
                        for (TypOsoba typOsoba : nalez.getOsoba()) {
                            model.add(DCTerms.creator, typOsoba.getCeleJmeno());
                        }
                    }
                }
            }
            if(obj instanceof TypMetadataPublikace) {
                TypMetadataPublikace metadataPublikace = ((TypMetadataPublikace) obj);

                //Creators
                if(metadataPublikace.getAutori() != null && metadataPublikace.getAutori().getAutorOrEditorOrPrekladatel() != null) {
                    for (JAXBElement<TypOsoba> typOsobaJAXBElement : metadataPublikace.getAutori().getAutorOrEditorOrPrekladatel()) {
                        TypOsoba osoba = typOsobaJAXBElement.getValue();
                        String localName = typOsobaJAXBElement.getName().getLocalPart();
                        if ("autor".equals(localName)) {
                            model.add(DCTerms.creator, osoba.getCeleJmeno());
                        } else { //editor, prekladatel, ilustrator
                            model.add(DCTerms.contributor, osoba.getCeleJmeno());
                        }
                    }
                }

                //Issued
                TypVydaniPublikace typVydaniPublikace = metadataPublikace.getVydani();
                if(typVydaniPublikace != null) {
                    addIfNotEmpty(model, DCTerms.identifier, "ISBN: ", typVydaniPublikace.getISBN());
                    addIfNotEmpty(model, DCTerms.identifier, "ISSN: ", typVydaniPublikace.getISSN());
                    addIfNotEmpty(model, DCTerms.publisher, typVydaniPublikace.getNakladatel());
                    if(typVydaniPublikace.getRokVydani() != null) {
                        addIfNotEmpty(model, DCTerms.issued, String.valueOf(typVydaniPublikace.getRokVydani().getYear()));
                    }
                    addIfNotEmpty(model, DCTerms.issued, String.valueOf(typVydaniPublikace.getMistoVydani()));

                    additionalMetadata.addIfNotEmpty("Edice", typVydaniPublikace.getEdice());
                    additionalMetadata.addIfNotEmpty("Označení vydání", typVydaniPublikace.getOznaceniVydani());
                    additionalMetadata.addIfNotEmpty("Počet výtisků", typVydaniPublikace.getPocetVytisku());
                    additionalMetadata.addIfNotEmpty("Rozsah", typVydaniPublikace.getRozsah());
                    additionalMetadata.addIfNotEmpty("Vytiskl", typVydaniPublikace.getVytiskl());
                }

                //Other
                TypUdajePublikace udajePublikace = metadataPublikace.getKnihovniUdaje();
                if(udajePublikace != null) {
                    addIfNotEmpty(model, DCTerms.language, udajePublikace.getJazykDokumentu());
                    addIfNotEmpty(model, DCTerms.title, udajePublikace.getNazev());
                    addIfNotEmpty(model, DCTerms.title, udajePublikace.getPodnazev());

                    additionalMetadata.addIfNotEmpty("Druh dokumentu", udajePublikace.getDruhDokumentu());
                    additionalMetadata.addIfNotEmpty("Signatura", udajePublikace.getSignatura());
                    additionalMetadata.addIfNotEmpty("Fond", udajePublikace.getFond());
                    additionalMetadata.addIfNotEmpty("Jmenné odkazy", udajePublikace.getJmenneOdkazy());
                    additionalMetadata.addIfNotEmpty("Věcné odkazy", udajePublikace.getVecneOdkazy());
                    additionalMetadata.addIfNotEmpty("Knihopisná poznámka", udajePublikace.getKnihopisnaPoznamka());
                    additionalMetadata.addIfNotEmpty("Předmětová hesla", udajePublikace.getPredmetovaHesla());
                    additionalMetadata.addIfNotEmpty("Rozpis svazku", udajePublikace.getRozpisSvazku());
                    additionalMetadata.addIfNotEmpty("Údaje o periodiku", udajePublikace.getUdajeOPeriodiku());
                }
            }
            if(obj instanceof TypMetadataOborova.OborovaMetadata) {
                List<Object> prirodovednaOrNumismaticka = ((TypMetadataOborova.OborovaMetadata) obj).getPrirodovednaOrNumismaticka();
                if(prirodovednaOrNumismaticka != null) {
                    for (Object o : prirodovednaOrNumismaticka) {
                        if (o instanceof TypMetadataPrirodovedna) {
                            TypMetadataPrirodovedna metadataPrirodovedna = (TypMetadataPrirodovedna) o;
                            TypTaxonomickaKategorie taxonomickaKategorie = metadataPrirodovedna.getTaxonomickaKategorie();
                            if(taxonomickaKategorie != null) {
                                StringBuilder taxonomySb = new StringBuilder();
                                accumulateIfNotEmpty(taxonomySb, "Říše: ", taxonomickaKategorie.getRise(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Kmen: ", taxonomickaKategorie.getKmen(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Oddělení: ", taxonomickaKategorie.getOddeleni(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Třída: ", taxonomickaKategorie.getTrida(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Řád: ", taxonomickaKategorie.getRad(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Čeleď: ", taxonomickaKategorie.getCeled(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Rod: ", taxonomickaKategorie.getRod(), "\n");
                                accumulateIfNotEmpty(taxonomySb, "Druh: ", taxonomickaKategorie.getDruh(), "\n");
                                addIfNotEmpty(model, DCTerms.description, taxonomySb.toString());
                            }
                        } else if (o instanceof TypMetadataNumismaticka) {
                            TypMetadataNumismaticka metadataNumismaticka = (TypMetadataNumismaticka) o;
                            TypNumismatickePopisy numismatickePopisy = metadataNumismaticka.getNumismatickePopisy();
                            if(numismatickePopisy != null) {
                                StringBuilder numismaticSb = new StringBuilder();
                                accumulateIfNotEmpty(numismaticSb, "Avers: ", numismatickePopisy.getAvers(), "\n");
                                accumulateIfNotEmpty(numismaticSb, "Hrana: ", numismatickePopisy.getHrana(), "\n");
                                accumulateIfNotEmpty(numismaticSb, "Revers: ", numismatickePopisy.getRevers(), "\n");
                                addIfNotEmpty(model, DCTerms.description, numismaticSb.toString());
                            }
                        }
                    }
                }
            }
        }

        additionalMetadata.fitToModel(model);
    }*/

    private void addIfNotEmpty(ZdoModel model, Property property, String val) {
        if(val != null && !val.isEmpty()) {
            model.add(property, val);
        }
    }

    private void addIfNotEmpty(ZdoModel model, Property property, String pre, String val) {
        if(val != null && !val.isEmpty()) {
            model.add(property, pre + val);
        }
    }

    private void accumulateIfNotEmpty(StringBuilder accumulator, String pre, String val, String post) {
        if(val != null && !val.isEmpty()) {
            accumulator.append(pre).append(val).append(post);
        }
    }

    public List<ZdoModel> parseFiles(SpecifickaMetadata specifickaMetadata, final ZdoModel parentModel, final File sourceFolder, final String invId) {
        List<ZdoModel> list = new ArrayList<>();
        List<SpecifickaMetadata.Kapitola.Zaznam.Subdata> subdataList = specifickaMetadata.getKapitola().getZaznam().getSubdata();
        for (SpecifickaMetadata.Kapitola.Zaznam.Subdata subdata : subdataList) {
            SpecifickaMetadata.Kapitola.Zaznam.Subdata.Obsah obsah = subdata.getObsah();
            String ucFileName = obsah.getNazevDd();

            ZdoModel pageModel = new ZdoModel(store.createUrl(UUID.randomUUID().toString()), ZdoType.page);
            pageModel.add(ZdoTerms.pageIndex, String.valueOf(obsah.getPozice()));
            pageModel.add(DCTerms.isPartOf, parentModel.getUrl());
            list.add(pageModel);

            if (ucFileName.startsWith("UC") || ucFileName.startsWith("MV")) {
                //We need to determine the zipfile that contains the file
                //It starts with our inventory id and ends with number same as last numbers of the mets entry
                // MV_MK_Film~1_01_0001.mpeg
                //will be found in something like
                // INV_ID_*01_0001.zip
                int dotPos = ucFileName.lastIndexOf(".");
                String matchingNumber = ucFileName.substring(dotPos-7, dotPos);
                File[] matchingFiles = sourceFolder.listFiles(
                        fileName -> fileName.getName().toLowerCase().endsWith(matchingNumber + ".zip")
                                && fileName.getName().startsWith(invId + "_"));
                if(matchingFiles.length != 1) {
                    throw new RuntimeException(matchingFiles.length + " files found when searching for zip containing " + ucFileName);
                }
                try {
                    ZipFile zipFile = new ZipFile(matchingFiles[0]);
                    guardianAngel.open();

                    //Now to find the right file inside the zip file
                    //Video files have been converted so their extensions are wrong, we need mp4
                    if(ucFileName.startsWith("MV")) {
                        ucFileName = ucFileName.substring(0, dotPos) + ".mp4";
                    }

                    ZipEntry zipEntry = zipFile.getEntry(ucFileName);
                    if (zipEntry == null) {
                        throw new RuntimeException("ZipEntry " + ucFileName + " was not found in " + matchingFiles[0]);
                    }

                    InputStream ucInputStream = zipFile.getInputStream(zipEntry);
                    String itemUrl = store.createUrl(UUID.randomUUID().toString());
                    ZdoModel ucModel = new ZdoModel(itemUrl, ucInputStream);
                    ucModel.setFileToClose(zipFile);    //remember zip file, it must be closed after inputstream is no longer needed
                    ucModel.add(DCTerms.isPartOf, pageModel.getUrl());
                    ucModel.add(ZdoTerms.fileType, ZdoFileType.userCopy.name());
                    ucModel.add(ZdoTerms.mimeType, findContentTypeInner(ucFileName));
                    list.add(ucModel);
                } catch (IOException e) {
                    throw new RuntimeException("Failed when getting user copy from zip file.", e);
                }
            }
            else {
                throw new RuntimeException("CHO file name not starting with UC, skipping, fix it.");
            }
        }

        return list;
    }

    private String findContentTypeInner(String filename) {
        if (filename.endsWith(".jp2")) {
            return "image/jp2";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
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
}
