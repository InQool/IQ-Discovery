package com.inqool.dcap.integration.z3950.sru.server;

import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import org.z3950.zing.cql.*;

import java.io.IOException;

/**
 * @author Lukas Jane (inQool) 1. 12. 2014.
 */
public class CqlToSparql {
    private StringBuilder prefixes;
    private StringBuilder filters;
    private int variableId = 0;

    public String convert(String cql) throws IOException, CQLParseException, MissingParameterException {
        CQLParser cqlParser = new CQLParser();
        CQLNode root = cqlParser.parse(cql);

        prefixes = new StringBuilder("");

        String base = "SELECT ?subject ?property ?value WHERE {\n" +
                "{ SELECT DISTINCT ?subject WHERE {\n" +
                "  ?subject ?p ?v.\n" +
                "?subject <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.ZDO.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.monograph.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.periodical.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.issue.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. }\n" +  //todo remove
                "";
        String custom = "";
        filters = new StringBuilder("");

        custom += parseNode(root);

        String end = "  FILTER ( " + filters + " ).\n" +
                "}\n}\n" +
                "?subject ?property ?value.\n" +
                "FILTER (regex(str(?property), \"http://purl.org/dc/terms/\")).\n" +
                "}";
        String all = prefixes + base + custom + end;


        //CQLNode root = cqlParser.parse(cql);
        return all;

    }

    private void addFilter(String filter) {
        if(!"".equals(filters.toString())) {
            filters.append(" && ");
        }
        filters.append(filter);
    }

    private String indexToProperty(String index) {
        if(index.startsWith("dc")) {
            if(!prefixes.toString().contains("PREFIX dcterms: <http://purl.org/dc/terms/>")) {
                prefixes.append("PREFIX dcterms: <http://purl.org/dc/terms/>\n");
            }
            return "dcterms:" + index.substring("dc.".length());
        }
        return index;
    }

    private String parseNode(CQLNode node) {
        if(node instanceof CQLAndNode) {
            CQLAndNode andNode = (CQLAndNode) node;
            return parseNode(andNode.getLeftOperand()) + parseNode(andNode.getRightOperand());
        }
        if(node instanceof CQLOrNode) {
            CQLOrNode orNode = (CQLOrNode) node;
            return "{\n" + parseNode(orNode.getLeftOperand()) + "} UNION {\n" + parseNode(orNode.getRightOperand()) + " }\n";
        }
        if(node instanceof CQLTermNode) {
            CQLTermNode termNode = (CQLTermNode) node;
            switch(termNode.getRelation().getBase()) {
                case "=":
                    addFilter("regex(str(?v" + ++variableId + "), \"" + termNode.getTerm() + "\")");
                    return "?subject " + indexToProperty(termNode.getIndex()) + " ?v" + variableId + ".\n";
                case "all":
                    StringBuilder result = null;
                    for(String word : termNode.getTerm().split(" ")) {
                        if(result == null) {
                            result = new StringBuilder();
                            result.append("( ");
                            ++variableId;
                        }
                        else {
                            result.append(" && ");
                        }
                        result.append("regex(str(?v")
                                .append(variableId)
                                .append("), \"")
                                .append(word)
                                .append("\")");
                    }
                    if(result != null) {
                        result.append(" )");
                        addFilter(result.toString());
                        return "?subject " + indexToProperty(termNode.getIndex()) + " ?v" + variableId + ".\n";
                    }
                    return "";
                case "any":
                    StringBuilder result2 = null;
                    for(String word : termNode.getTerm().split(" ")) {
                        if(result2 == null) {
                            result2 = new StringBuilder();
                            result2.append("( ");
                            ++variableId;
                        }
                        else {
                            result2.append(" || ");
                        }
                        result2.append("regex(str(?v")
                                .append(variableId)
                                .append("), \"")
                                .append(word)
                                .append("\")");
                    }
                    if(result2 != null) {
                        result2.append(" )");
                        addFilter(result2.toString());
                        return "?subject " + indexToProperty(termNode.getIndex()) + " ?v" + variableId + ".\n";
                    }
                    return "";
                case "exact":
                    return "?subject " + indexToProperty(termNode.getIndex()) + " " + termNode.getTerm() + ".\n";
                default:
            }
            return indexToProperty(termNode.getIndex()) + termNode.getRelation() + termNode.getTerm();
        }
        return "";
    }
}
