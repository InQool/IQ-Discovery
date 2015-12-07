package com.inqool.dcap;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 11. 3. 2015.
 */
@ApplicationScoped
public class SparqlTools {

    public Map<String, Map<String, List<String>>> queryExecutionToPropertyMap(QueryExecution queryExecution) {
        //Go through all triples and reconstruct property map
        Map<String, Map<String, List<String>>> subjects = new LinkedHashMap<>();
        ResultSet rs = queryExecution.execSelect();
        while(rs.hasNext()) {
            QuerySolution querySolution = rs.next();
            String subject = querySolution.getResource("subject").getURI();
            if(!subjects.containsKey(subject)) {
                subjects.put(subject, new HashMap<>());
            }
            String property = querySolution.getResource("property").getURI();
            if(!subjects.get(subject).containsKey(property)) {
                List<String> valueList = new ArrayList<>();
                subjects.get(subject).put(property, valueList);
            }
            RDFNode value = querySolution.get("value");
            String strValue;
            if(value.isLiteral()) {
                strValue = value.asLiteral().getString();
            }
            else if(value.isResource()) {
                strValue = value.asResource().getURI();
            }
            else {
                throw new RuntimeException("Expected literal or resource");
            }
            subjects.get(subject).get(property).add(strValue);
        }
        return subjects;
    }

    public String createRootTypeCondition() {
        //Filter documents by type
        String typeCondition = "";
        boolean firstIter = true;
        for(ZdoType type : ZdoType.values()) {
            if(ZdoType.isRootCategory(type.name())) {
                if(firstIter) {
                    firstIter = false;
                }
                else {
                    typeCondition += " UNION \n";
                }
                typeCondition += "{ ?subject <" + ZdoTerms.zdoType.getURI() + "> " + ZdoTerms.stringConstantOf(type.name()) + ". }\n";
            }
        }
        return typeCondition;
    }

    public String createEndBranchTypeCondition() {
        //Filter documents by type
        String typeCondition = "{\n";
        boolean firstIter = true;
        for(ZdoType type : ZdoType.values()) {
            if(ZdoType.isBranchEndCategory(type.name())) {
                if(firstIter) {
                    firstIter = false;
                }
                else {
                    typeCondition += "} UNION {\n";
                }
                typeCondition += "?subject <" + ZdoTerms.zdoType.getURI() + "> " + ZdoTerms.stringConstantOf(type.name()) + ".";
            }
        }
        typeCondition += "\n}\n";
        return typeCondition;
    }

    public String createAbovePageTypeCondition() {
        //Filter documents by type
        String typeCondition = "{\n";
        boolean firstIter = true;
        for(ZdoType type : ZdoType.values()) {
            if(ZdoType.isAbovePageCategory(type.name())) {
                if(firstIter) {
                    firstIter = false;
                }
                else {
                    typeCondition += "} UNION {\n";
                }
                typeCondition += "?subject <" + ZdoTerms.zdoType.getURI() + "> " + ZdoTerms.stringConstantOf(type.name()) + ".";
            }
        }
        typeCondition += "\n}\n";
        return typeCondition;
    }

    public String createDocumentStateCondition(String state) {
        String stateConditions = "";
        switch(state) {
            case "original":
                stateConditions +=
                        "?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ".\n";
                break;
            case "concept":
                stateConditions +=
                        "?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ".\n";
                break;
            case "published":
                stateConditions +=
                        "?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO.name()) + ".\n";
                break;
            case "notOriginal":
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO.name()) + ". }";
                break;
            case "notConcept":
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO.name()) + ". }";
                break;
            case "notPublished":
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ". }";
                break;
            case "all":
            default:
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO.name()) + ". }";
        }
        return stateConditions;
    }

    public String createDocumentStateConditionIncludingOthers(String state) {
        String stateConditions = "";
        switch(state) {
            case "original":
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.BACH.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.DEMUS.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.EXTERNAL.name()) + ". }";
                break;
            case "concept":
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO_CONCEPT.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.BACH.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.DEMUS.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.EXTERNAL.name()) + ". }";
                break;
            case "published":
                stateConditions +=
                        "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.ZDO.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.KDR.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.BACH.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.DEMUS.name()) + ". } UNION" +
                                "{ ?subject <" + ZdoTerms.group + "> " + ZdoTerms.stringConstantOf(ZdoGroup.EXTERNAL.name()) + ". }";
                break;
        }
        return stateConditions;
    }

    //Filter documents by inventory id
    public String createInventoryIdProxCondition(String inputText) {
        String filterCondition = "";
        if(inputText != null && !inputText.isEmpty()) {
            inputText = inputText.replace("*", "");
            String[] inventoryIdsSplitted = inputText.split(",");
            List<String> inventoryIdList = new ArrayList<>();
            for(String inventoryId : inventoryIdsSplitted) {
                inventoryId = inventoryId.toUpperCase().trim();
                inventoryId = tokenizeSpaces(inventoryId);
                inventoryIdList.add(inventoryId);
            }

            //Add regex for every inventory id
            String filter = "FILTER (\n";
            boolean isFirst = true;
            for(String inventoryId : inventoryIdList) {
                if(!isFirst) {
                    filter += " ||\n";
                }
                else {
                    isFirst = false;
                }
                filter += "  regex(str(?v), \"" + inventoryId + "\")";
            }
            filter += "\n).\n";

            filterCondition =
                    "{ SELECT DISTINCT ?subject WHERE {\n" +
                            "  ?subject <http://inqool.cz/zdo/1.0/inventoryId> ?v.\n" +
                            filter +
                            "}}";
        }
        return filterCondition;
    }

    public String tokenizeSpaces(String input) {
        //This adds "\\s*" between all letters in the word, and it means an optional space anywhere
        return input.replaceAll("\\B", "\\\\\\\\s*");
    }

    public String createInventoryIdQuery(String inventoryId) {
        String filterCondition = "";
        if(inventoryId != null && !inventoryId.isEmpty()) {
            filterCondition =
                    "{ SELECT DISTINCT ?subject WHERE {\n" +
                    createInventoryIdCondition(inventoryId) +
                    "}}";
        }
        return filterCondition;
    }

    public String createInventoryIdCondition(String inventoryId) {
        return "  ?subject <" + ZdoTerms.inventoryId.getURI() + "> " + ZdoTerms.stringConstantOf(inventoryId.toUpperCase()) + ".\n";
    }

    public String createBatchCondition(Integer batch) {
        String batchCondition = "";
        if(batch != null) {
            batchCondition = "?subject <http://inqool.cz/zdo/1.0/batchId> " + ZdoTerms.stringConstantOf(String.valueOf(batch)) + ".";
        }
        return batchCondition;
    }

    public String createOrganizationCondition(String org) {
        String orgCondition = "";
        if(org != null) {
            orgCondition = "  ?subject <http://inqool.cz/zdo/1.0/organization> " + ZdoTerms.stringConstantOf(org) + ".\n";
        }
        return orgCondition;
    }

    public String createOwnershipCondition(String loginName) {
        String ownerCondition = "";
        if(loginName != null) {
            ownerCondition = "  {\n" +  //For models that have no parent
                    "    {\n" +   //select models that are owned by user
                    "      ?subject <http://inqool.cz/zdo/1.0/owner> " + ZdoTerms.stringConstantOf(loginName) + ".\n" +
                    "    } UNION {\n" +   //or those that have no owner
                    "      ?subject <http://inqool.cz/zdo/1.0/group> ?val1.\n" +  //select all models
                    "      MINUS {\n" +
                    "        ?subject <http://inqool.cz/zdo/1.0/owner> ?otherOwner.\n" +  //but subtract those that have owners
                    "      }\n" +
                    "    }\n" +
                    "    MINUS {\n" +   //no parent
                    "      ?subject <http://purl.org/dc/terms/isPartOf> ?some.\n" +
                    "    }\n" +
                    //For those that have parent, check owner of grandparent (issue -> check periodical)
                    "  } UNION {\n" +
                    "    ?subject <http://purl.org/dc/terms/isPartOf> ?volume.\n" +
                    "    ?volume <http://purl.org/dc/terms/isPartOf> ?periodical.\n" +
                    "    { \n" +    //select periodicals that are owned by user
                    "      ?periodical <http://inqool.cz/zdo/1.0/owner> " + ZdoTerms.stringConstantOf(loginName) + ".\n" +
                    "    } UNION {\n" + //or those that have no owner
                    "      ?periodical <http://inqool.cz/zdo/1.0/group> ?val2. \n" +    //again, select all periodicals
                    "      MINUS {\n" +
                    "        ?periodical <http://inqool.cz/zdo/1.0/owner> ?otherOwner. \n" +    //but subtract those that have owners
                    "      }\n" +
                    "    } \n" +
                    "  }\n";
        }
        return ownerCondition;
    }
}
