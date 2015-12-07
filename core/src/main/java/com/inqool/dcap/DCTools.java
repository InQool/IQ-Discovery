package com.inqool.dcap;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DC_11;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 9. 1. 2015.
 */
public class DCTools {
    private static final List<Property> dcElementPropertyList = Arrays.asList(
            DC_11.contributor,
            DC_11.coverage,
            DC_11.creator,
            DC_11.date,
            DC_11.description,
            DC_11.format,
            DC_11.identifier,
            DC_11.language,
            DC_11.publisher,
            DC_11.relation,
            DC_11.rights,
            DC_11.source,
            DC_11.subject,
            DC_11.title,
            DC_11.type
    );

    private static final List<String> dcElementList = Arrays.asList(
            "contributor",
            "coverage",
            "creator",
            "date",
            "description",
            "format",
            "identifier",
            "language",
            "publisher",
            "relation",
            "rights",
            "source",
            "subject",
            "title",
            "type"
    );

    private static final List<String> dcTermList = Arrays.asList(
            "abstract",
            "accessRights",
            "accrualMethod",
            "accrualPeriodicity",
            "accrualPolicy",
            "alternative",
            "audience",
            "available",
            "bibliographicCitation",
            "conformsTo",
            "contributor",
            "coverage",
            "created",
            "creator",
            "date",
            "dateAccepted",
            "dateCopyrighted",
            "dateSubmitted",
            "description",
            "educationLevel",
            "extent",
            "format",
            "hasFormat",
            "hasPart",
            "hasVersion",
            "identifier",
            "instructionalMethod",
            "isFormatOf",
            "isPartOf",
            "isReferencedBy",
            "isReplacedBy",
            "isRequiredBy",
            "issued",
            "isVersionOf",
            "language",
            "license",
            "mediator",
            "medium",
            "modified",
            "provenance",
            "publisher",
            "references",
            "relation",
            "replaces",
            "requires",
            "rights",
            "rightsHolder",
            "source",
            "spatial",
            "subject",
            "tableOfContents",
            "temporal",
            "title",
            "type",
            "valid"
    );

    private static final List<String> translateTerms = Arrays.asList(
            "created" //todo fill
    );
    private static final List<String> translateTo = Arrays.asList(
            "date" //todo fill at the same time
    );

    public static String dcTermToDcElement(String term) {
        int position = translateTerms.indexOf(term);
        if(position == -1) return null;
        return translateTo.get(position);
    }

    public static List<String> getDcTermList() {
        return dcTermList;
    }

    public static List<String> getDcElementList() {
        return dcElementList;
    }

    public static List<String> getTranslatableTerms() {
        return translateTerms;
    }
}
