package com.inqool.dcap.integration.z3950.sru.server;

import com.inqool.dcap.integration.z3950.sru.server.config.IndexConversionMapping;
import com.inqool.dcap.integration.z3950.sru.server.config.SruDiagnosticsConstants;
import org.oasis_open.docs.ns.search_ws.diagnostic.DiagnosticComplexType;
import org.z3950.zing.cql.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 1. 12. 2014.
 */
@Dependent
public class CqlToSolr {

    private List<DiagnosticComplexType> diagnostics;

    @Inject
    private IndexConversionMapping indexConversionMapping;

    /**
     *
     * @param cql CQL query string to be converted to Solr
     * @param diagnostics instantiated list that will be filled with any encountered error diagnostics
     * @return Lucene/Solr compatible query string
     * @throws CouldNotParseCqlException in case that query was not parsed at all and should not be put to Solr
     */
    public String convert(String cql, List<DiagnosticComplexType> diagnostics) throws CouldNotParseCqlException {
        this.diagnostics = diagnostics;
        CQLParser cqlParser = new CQLParser();
        CQLNode root;
        try {
            root = cqlParser.parse(cql);
        }
        catch (CQLParseException | IOException e) {
            addDiagnostics(SruDiagnosticsConstants.QUERY_SYNTAX_ERROR, "", "Query syntax error.");
            throw new CouldNotParseCqlException("Query syntax error.");
        }

        String result = parseNode(root);
        return result;
    }

    /**
     * Recursively parses CQL node and returns its Solr equivalent
     * @param node
     * @return
     * @throws CouldNotParseCqlException
     */
    private String parseNode(CQLNode node) throws CouldNotParseCqlException {
        if(node instanceof CQLSortNode) {
            addDiagnostics(SruDiagnosticsConstants.QUERY_FEATURE_UNSUPPORTED, "sortBy", "Sorting not supported");
            CQLSortNode sortNode = (CQLSortNode) node;
            return parseNode(sortNode.getSubtree());
        }
        if(node instanceof CQLProxNode) {
            addDiagnostics(SruDiagnosticsConstants.PROXIMITY_NOT_SUPPORTED, "prox", "Proximity not supported");
            CQLProxNode proxNode = (CQLProxNode) node;
            return "(" + parseNode(proxNode.getLeftOperand()) + " AND " + parseNode(proxNode.getRightOperand()) + ")";  //mask PROX as AND
        }
        if(node instanceof CQLAndNode) {
            CQLAndNode andNode = (CQLAndNode) node;
            for(Modifier modifier : andNode.getModifiers()) {
                addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_BOOLEAN_MODIFIER, modifier.getValue(), "Modifier not supported.");
            }
            return "(" + parseNode(andNode.getLeftOperand()) + " AND " + parseNode(andNode.getRightOperand()) + ")";
        }
        if(node instanceof CQLOrNode) {
            CQLOrNode orNode = (CQLOrNode) node;
            for(Modifier modifier : orNode.getModifiers()) {
                addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_BOOLEAN_MODIFIER, modifier.getValue(), "Modifier not supported.");
            }
            return "(" + parseNode(orNode.getLeftOperand()) + " OR " + parseNode(orNode.getRightOperand()) + ")";
        }
        if(node instanceof CQLNotNode) {
            CQLNotNode notNode = (CQLNotNode) node;
            for(Modifier modifier : notNode.getModifiers()) {
                addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_BOOLEAN_MODIFIER, modifier.getValue(), "Modifier not supported.");
            }
            return "(" + parseNode(notNode.getLeftOperand()) + " NOT " + parseNode(notNode.getRightOperand()) + ")";
        }
        if(node instanceof CQLTermNode) {
            CQLTermNode termNode = (CQLTermNode) node;
            for(Modifier modifier : termNode.getRelation().getModifiers()) {
                addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_RELATION_MODIFIER, modifier.getValue(), "Modifier not supported.");
            }
            switch(termNode.getRelation().getBase()) {
                case "=":
                    return indexToProperty(termNode.getIndex()) + termitize(termNode.getTerm());
                case "all":
                    StringBuilder alls = new StringBuilder("");
                    for(String word : termNode.getTerm().split(" ")) {
                        alls.append(" +")
                                .append(termitize(word));
                    }
                    return indexToProperty(termNode.getIndex()) + "(" + alls + ")";
                case "any":
                    StringBuilder anies = new StringBuilder("");
                    for(String word : termNode.getTerm().split(" ")) {
                        anies.append(" ")
                                .append(termitize(word));
                    }
                    return indexToProperty(termNode.getIndex()) + "(" + anies + ")";
                case "exact":
                case "==":
                    return indexToProperty(termNode.getIndex()) + termitize(termNode.getTerm());
                default:
                    addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_RELATION, termNode.getRelation().getBase(), "Relation not supported.");
                    throw new CouldNotParseCqlException("Relation not supported.");
            }
        }
        return "";
    }

    /**
     * For CQL index, returns Solr property
     * @param index
     * @return
     * @throws CouldNotParseCqlException
     */
    private String indexToProperty(String index) throws CouldNotParseCqlException {
        //Search everywhere
        if(index.startsWith("cql.serverChoice") || index.startsWith("cql.allIndexes")) {
            return "";
        }
        //Split
        String set;
        String property;
        if(index.contains(".")) {
            set = index.substring(0, index.indexOf("."));
            property = index.substring(index.indexOf(".")+1);
        }
        else {
            set = "";
            property = index;
        }
        //Ensure set is supported
        if(!indexConversionMapping.getMapping().containsKey(set)) {
            addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_CONTEXT_SET, set, "Unsupported context set.");
            throw new CouldNotParseCqlException("Unsupported context set.");
        }
        //Ensure index is supported
        if(!indexConversionMapping.getMapping().get(set).containsKey(property)) {
            addDiagnostics(SruDiagnosticsConstants.UNSUPPORTED_INDEX, property, "Unsupported index.");
            throw new CouldNotParseCqlException("Unsupported index.");
        }
        //Set index so that it matches solr index
        return indexConversionMapping.getMapping().get(set).get(property)+":";
        //fallbacks
/*        if(index.contains(".")) {
            return index.substring(index.indexOf(".") + 1) + ":";
        }
        return index+":";*/
    }

    /**
     * Makes sure string is surrounded by double quotes
     * @param term string or "string"
     * @return "string"
     */
    private static String termitize(String term) {
        if(term.startsWith("\"") && term.endsWith("\"")) return term;
        if(term.contains(" ")) return "\"" + term + "\"";
        return term;
    }

    /**
     * Adds error diagnostics to future response
     * @param number
     * @param details
     * @param message
     */
    private void addDiagnostics(int number, String details, String message) {
        if(diagnostics == null) diagnostics = new ArrayList<>();
        {
            DiagnosticComplexType diagnostic = new DiagnosticComplexType();
            diagnostic.setUri(SruDiagnosticsConstants.DIAGNOSTIC_URI_PREFIX + number);
            diagnostic.setDetails(details);
            diagnostic.setMessage(message);
            diagnostics.add(diagnostic);
        }
    }
}
