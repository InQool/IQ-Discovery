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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.Lambda;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.jena.type.*;
import gov.loc.mods.v3.*;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBElement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses Mods XML document and update provided RDF model.
 *
 * fixme: constructCreator add all relevant codes for author
 *
 * @author Matus Zamborsky (inQool)
 */
@ApplicationScoped
public class ModsParser {
    public ZdoModel parse(final Mods mods, final ZdoModel model) {
        Resource subject = model.getSubject();

//        AditionalMetadata aditionalMetadata = AditionalMetadata.ofModel(model);

        constructTitle(mods).forEach(title -> model.add(subject, DCTerms.title, title));
        /*constructAlternative(mods).forEach(title -> model.add(subject, DCTerms.alternative, title));*/
        constructAlternative(mods).forEach(title -> model.add(subject, DCTerms.title, title));
        constructLanguage(mods).forEach(language -> model.add(subject, DCTerms.language, language, ISO6392DataType.get()));
        constructSpatial(mods, "czenas").forEach(spatial -> model.add(subject, DCTerms.spatial, spatial, CzenasDataType.get()));
        constructTopic(mods, "czenas").forEach(topic -> model.add(subject, DCTerms.subject, topic, CzenasDataType.get()));
        constructTemporal(mods, "czenas").forEach(temporal -> model.add(subject, DCTerms.temporal, temporal, CzenasDataType.get()));
        constructGenre(mods, "czenas").forEach(genre -> model.add(subject, DCTerms.type, genre, CzenasDataType.get()));
        constructClassification(mods, "udc").forEach(classification -> model.add(subject, DCTerms.subject, classification, UDCDataType.get()));
        constructCreator(mods).forEach(creator -> model.add(subject, DCTerms.creator, creator));
        constructPublisher(mods).forEach(publisher -> model.add(subject, DCTerms.publisher, publisher));

        constructNote(mods).forEach(note -> model.add(subject, DCTerms.description, note));

        constructCreated(mods, "marc").forEach(created -> model.add(subject, DCTerms.created, created, MarcDataType.get()));
        constructCreated(mods, null).forEach(created -> model.add(subject, DCTerms.created, created));

        constructIdentifier(mods, "uuid").forEach(identifier -> model.add(subject, DCTerms.identifier, identifier, UUIDataType.get()));
        constructIdentifier(mods, "urnnbn").forEach(identifier -> model.add(subject, DCTerms.identifier, identifier, UrnNbnDataType.get()));
        constructIdentifier(mods, "issn").forEach(identifier -> model.add(subject, DCTerms.identifier, identifier, ISSNDataType.get()));
        constructIdentifier(mods, "ccnb").forEach(identifier -> model.add(subject, DCTerms.identifier, identifier, CCNBDataType.get()));
        constructIdentifier(mods, "isbn").forEach(identifier -> model.add(subject, DCTerms.identifier, identifier, ISBNDataType.get()));

        constructReplaces(mods).forEach(replaces -> model.add(subject, DCTerms.replaces, replaces));
        constructReplacesISSN(mods).forEach(replaces -> model.add(subject, DCTerms.replaces, replaces, ISSNDataType.get()));

        constructReplacedBy(mods).forEach(replaced -> model.add(subject, DCTerms.isReplacedBy, replaced));
        constructReplacedByISSN(mods).forEach(replaced -> model.add(subject, DCTerms.isReplacedBy, replaced, ISSNDataType.get()));

//        aditionalMetadata.fitToModel(model);
        return model;
    }

    private Stream<String> constructReplaces(final Mods mods) {
        return getModsElement(mods, RelatedItem.class)
                .filter(item -> parseRelatedItemIdentifier(item, "preceding", "issn").isPresent())
                .map(item -> parseRelatedItem(item, "preceding"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructReplacesISSN(final Mods mods) {
        return getModsElement(mods, RelatedItem.class)
                .map(item -> parseRelatedItemIdentifier(item, "preceding", "issn"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructReplacedBy(final Mods mods) {
        return getModsElement(mods, RelatedItem.class)
                .filter(item -> parseRelatedItemIdentifier(item, "succeeding", "issn").isPresent())
                .map(item -> parseRelatedItem(item, "succeeding"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructReplacedByISSN(final Mods mods) {
        return getModsElement(mods, RelatedItem.class)
                .map(item -> parseRelatedItemIdentifier(item, "succeeding", "issn"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructIdentifier(final Mods mods, final String type) {
        return getModsElement(mods, Identifier.class)
                .map(identifier -> this.parseIdentifier(identifier, type))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructNote(final Mods mods) {
        return getModsElement(mods, Note.class)
                .map(this::parseNote)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructClassification(final Mods mods, String authority) {
        return getModsElement(mods, Classification.class)
                .map(classification -> this.parseClassification(classification, authority))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructGenre(final Mods mods, final String authority) {
        return getModsElement(mods, Genre.class)
                .map(genre -> this.parseGenre(genre, authority))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructTitle(final Mods mods) {
        return getModsElement(mods, TitleInfo.class)
                .filter(titleInfo -> titleInfo.getModsType() == null)
                .map(this::parseTitleInfo);
    }

    private Stream<String> constructAlternative(final Mods mods) {
        return getModsElement(mods, TitleInfo.class)
                .filter(titleInfo -> TitleInfoTypeAttributeDefinition.ALTERNATIVE == titleInfo.getModsType())
                .map(this::parseTitleInfo);
    }


    private Stream<String> constructLanguage(final Mods mods) {
        return getModsElement(mods, LanguageDefinition.class)
                .map(this::parseLanguage)
                .flatMap(language -> language);
    }

    private Stream<String> constructSpatial(final Mods mods, String authority) {
        return getModsElement(mods, Subject.class)
                .filter(subject -> authority.equals(subject.getAuthority()))
                .map(subject -> parseSubject(subject, "geographic"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructTopic(final Mods mods, String authority) {
        return getModsElement(mods, Subject.class)
                .filter(subject -> authority.equals(subject.getAuthority()))
                .map(subject -> parseSubject(subject, "topic"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructTemporal(final Mods mods, String authority) {
        return getModsElement(mods, Subject.class)
                .filter(subject -> authority.equals(subject.getAuthority()))
                .map(subject -> parseSubject(subject, "temporal"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructCreator(final Mods mods) {
        return getModsElement(mods, Name.class)
                .filter(name -> NameTypeAttributeDefinition.PERSONAL == name.getModsType())
                .filter(name -> isInRole(name, Arrays.asList("cre", "aut")))
                .map(this::parseName)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructCreated(final Mods mods, String encoding) {
        return getModsElement(mods, OriginInfo.class)
                .map(info -> parseOriginInfoDate(info, Arrays.asList("dateCreated", "dateIssued"), encoding))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<String> constructPublisher(final Mods mods) {
        return getModsElement(mods, OriginInfo.class)
                .flatMap(this::parseOriginInfoPublisher);
    }

    private Stream<String> parseOriginInfoPublisher(final OriginInfo info) {
        return Lambda.stream(info.getPlacesAndPublishersAndDateIssueds())
                .filter(obj -> obj instanceof JAXBElement)
                .map(obj -> (JAXBElement) obj)
                .filter(obj -> Objects.equals("publisher", obj.getName().getLocalPart()))
                .filter(obj -> obj.getValue() instanceof StringPlusSupplied)
                .map(obj -> (StringPlusSupplied) obj.getValue())
                .map(XsString::getValue);
    }

    private Optional<String> parseOriginInfoDate(final OriginInfo info, final List<String> types, final String encoding) {
        return Lambda.stream(info.getPlacesAndPublishersAndDateIssueds())
                .filter(obj -> obj instanceof JAXBElement)
                .map(obj -> (JAXBElement)obj)
                .filter(obj -> types.contains(obj.getName().getLocalPart()))
                .filter(obj -> obj.getValue() instanceof DateDefinition)
                .map(obj -> (DateDefinition)obj.getValue())
                .filter(date -> Objects.equals(date.getEncoding(), encoding))
                .map(XsString::getValue)
                .findFirst();
    }

    private Optional<String> parseName(final Name name) {
        return Lambda.stream(name.getNamePartsAndDisplayFormsAndAffiliations())
                .filter(obj -> obj instanceof NamePart)
                .map(obj -> (NamePart)obj)
                .filter(part -> part.getType() == null)
                .map(XsString::getValue)
                .findFirst();
    }

    private Optional<String> parseSubject(final Subject subject, final String type) {
        return Lambda.stream(subject.getTopicsAndGeographicsAndTemporals())
                .filter(obj -> obj instanceof JAXBElement)
                .map(obj -> (JAXBElement) obj)
                .filter(obj -> Objects.equals(obj.getName().getLocalPart(), type))
                .filter(obj -> obj.getValue() instanceof StringPlusAuthority)
                .map(obj -> (StringPlusAuthority) obj.getValue())
                .map(XsString::getValue)
                .findFirst();
    }

    private Stream<String> parseLanguage(final LanguageDefinition language) {
        return Lambda.stream(language.getLanguageTerms())
                .map(this::parseLanguageTerm)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<String> parseLanguageTerm(final LanguageTerm langTerm) {
        return Optional.of(langTerm)
                .filter(term -> CodeOrTextDefinition.CODE == term.getType())
                .filter(term -> LanguageAuthorityAttributeDefinition.ISO_639_2_B == term.getAuthority())
                .map(XsString::getValue);
    }

    private String parseTitleInfo(final TitleInfo info) {
        return Lambda.stream(info.getTitlesAndSubTitlesAndPartNumbers())
                .map(element -> element.getValue().getValue())
                .collect(Collectors.joining(" "));
    }

    private Optional<String> parseIdentifier(final Identifier identifier, final String idType) {
        return Optional.of(identifier)
                .map(Identifier::getType)
                .filter(idType::equals)
                .map(type -> identifier.getValue());
    }

    private Optional<String> parseNote(final Note note) {
        return Optional.of(note.getValue());
    }

    private Optional<String> parseClassification(final Classification classification, String auth) {
        return Optional.of(classification.getAuthority())
                .filter(auth::equals)
                .map(authority -> classification.getValue());
    }

    private Optional<String> parseGenre(final Genre genre, final String auth) {
        return Optional.ofNullable(genre.getAuthority())
                .filter(auth::equals)
                .map(authority -> genre.getValue());
    }

    private Optional<String> parseRelatedItem(final RelatedItem relatedItem,final String type) {
        return Stream.of(relatedItem)
                .filter(item -> Objects.equals(type, item.getType()))
                .flatMap(item -> getModsElement(item.getAbstractsAndAccessConditionsAndClassifications(), TitleInfo.class)
                                .map(this::parseTitleInfo)
                )
                .findFirst();
    }

    private Optional<String> parseRelatedItemIdentifier(final RelatedItem relatedItem,final String type, final String idType) {
         return Stream.of(relatedItem)
                .filter(item -> Objects.equals(type, item.getType()))
                .flatMap(item -> getModsElement(item.getAbstractsAndAccessConditionsAndClassifications(), Identifier.class))
                .map(identifier -> this.parseIdentifier(identifier, idType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }


    private boolean isInRole(final Name name, final List<String> roles) {
        return Lambda.stream(name.getNamePartsAndDisplayFormsAndAffiliations())
                .filter(obj -> obj instanceof Role)
                .map(obj -> (Role)obj)
                .filter(role -> isInRole(role, roles))
                .count() > 0;
    }

    private boolean isInRole(final Role role, final List<String> roles) {
        return Lambda.stream(role.getRoleTerms())
                .filter(term -> isInRole(term, roles))
                .count() > 0;
    }

    private boolean isInRole(final RoleTerm roleTerm, final List<String> roles) {
        return Optional.of(roleTerm)
                .filter(term -> CodeOrTextDefinition.CODE == term.getType())
                .filter(term -> "marcrelator".equals(term.getAuthority()))
                .filter(term -> roles.contains(roleTerm.getValue()))
                .isPresent();
    }

    private <T> Stream<T> getModsElement(final Mods mods, final Class<? extends T> type) {
        return getModsElement(mods.getAbstractsAndAccessConditionsAndClassifications(), type);
    }


    @SuppressWarnings("unchecked")
    private <T> Stream<T> getModsElement(final List<?> objects, final Class<? extends T> type) {
        return Lambda.stream(objects)
                .filter(object -> object.getClass().equals(type))
                .map(object -> (T) object);
    }
}
