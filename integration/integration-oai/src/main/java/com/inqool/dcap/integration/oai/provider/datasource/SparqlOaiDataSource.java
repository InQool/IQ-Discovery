package com.inqool.dcap.integration.oai.provider.datasource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.oai.provider.exception.IdDoesNotExistPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoRecordsMatchPmhException;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation that reads records from Triplestore using Sparql query language
 * @author Lukas Jane (inQool) 2. 5. 2015.
 */
@RequestScoped
public class SparqlOaiDataSource implements OaiDataSource {
    @Inject
    @ConfigProperty(name = "fedora.endpoint")
    private String FEDORA_ENDPOINT;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    //Number of records returned at once during OAI list requests
    private static final int PAGE_SIZE = 1000;

    @Override
    public Map<String, List<String>> getRecordDataToMap(String identifier) throws IdDoesNotExistPmhException {
        Map<String, List<String>> resultMap = new HashMap<>();
        String invId = oaiIdToInvId(identifier);
        String queryString = "SELECT ?predicate ?value WHERE {" +
                "?subject ?predicate ?value." +
                "?subject <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.ZDO.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>." +
                "?subject <" + ZdoTerms.inventoryId.getURI() + "> \"" + invId + "\"^^<http://www.w3.org/2001/XMLSchema#string>." +
                "{ ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.monograph.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. } UNION" +
                "{ ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.periodical.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. } UNION" +
                "{ ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.cho.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. } UNION" +
                "{ ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.bornDigital.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. } " +
                " }";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(!resultSet.hasNext()) {
            throw new IdDoesNotExistPmhException();
        }
        while(resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.next();
            String property = querySolution.getResource("predicate").getLocalName();
            String val;
            RDFNode valueNode = querySolution.get("value");
            if(valueNode.isLiteral()) {
                val = valueNode.asLiteral().getString();
            }
            else {
                val = valueNode.asResource().getURI();
            }
            if("lastModifed".equals(property)) {
                val = val.substring(0, val.indexOf("T"));
            }
            if(resultMap.containsKey(property)) {
                resultMap.get(property).add(val);
            }
            else {
                List<String> valList = new ArrayList<>();
                valList.add(val);
                resultMap.put(property, valList);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Map<String, List<String>>> listRecordDataToMap(OffsetDateTime from, OffsetDateTime until, String set, int page, AtomicBoolean hasMore) throws NoRecordsMatchPmhException {
        String queryString = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT ?subject ?property ?value ?invId WHERE {\n" +
                "{ SELECT DISTINCT ?subject WHERE {\n" +
                "  ?subject ?p ?v.\n" +
                "  ?subject <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.ZDO.name() + "\"^^xsd:string.\n" +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.monograph.name() + "\"^^xsd:string. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.periodical.name() + "\"^^xsd:string. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.cho.name() + "\"^^xsd:string. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.bornDigital.name() + "\"^^xsd:string. }\n" +
                "  ?subject <http://fedora.info/definitions/v4/repository#lastModified> ?modifiedTime.\n" +
                "  FILTER (\"" + from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\"^^xsd:dateTime <= ?modifiedTime && ?modifiedTime <= \"" + until.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\"^^xsd:dateTime).\n" +
                "  } ORDER BY ?modifiedTime LIMIT " + (PAGE_SIZE + 1) + " OFFSET " + page*PAGE_SIZE + "\n}\n" + //we retrieve one more to determine if there are more records after last one we send
                "?subject ?property ?value.\n" +
                "?subject <" + ZdoTerms.inventoryId.getURI() + "> ?invId.\n" +
                " }";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);

        //this would be easier but it never worked for me:
        /*Model model = queryExecution.execConstruct();*/
        //so using workaround below

        //Go through all triples and reconstruct property map
        ResultSet resultSet = queryExecution.execSelect();
        if(!resultSet.hasNext()) {
            throw new NoRecordsMatchPmhException();
        }
        int counter = 0;
        Map<String, Map<String, List<String>>> subjects = new HashMap<>();
        while(resultSet.hasNext()) {
            //We set that resumption token should be issued, and rest of the records will be returned next time
            if (++counter > PAGE_SIZE) {
                hasMore.set(true);
                break;
            }
            QuerySolution querySolution = resultSet.next();
            String subject = invIdToOaiId(querySolution.getLiteral("invId").getString());
            if(!subjects.containsKey(subject)) {
                subjects.put(subject, new HashMap<>());
            }
            String property = querySolution.getResource("property").getLocalName();
            if(!subjects.get(subject).containsKey(property)) {
                subjects.get(subject).put(property, new ArrayList<>());
            }
            RDFNode valueNode = querySolution.get("value");
            String dcVal;
            if(valueNode.isLiteral()) {
                dcVal = valueNode.asLiteral().getString();
            }
            else {
                dcVal = valueNode.asResource().getURI();
            }
            subjects.get(subject).get(property).add(dcVal);
        }
        return subjects;
    }

    @Override
    public Map<String, String> listIdentifiersToMap(OffsetDateTime from, OffsetDateTime until, String set, int page, AtomicBoolean hasMore) throws NoRecordsMatchPmhException {
        String queryString = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT ?subject ?modifiedTime ?invId WHERE {\n" +
                "  ?subject <http://fedora.info/definitions/v4/repository#lastModified> ?modifiedTime.\n" +
                "  ?subject <" + ZdoTerms.inventoryId.getURI() + "> ?invId.\n" +
                "  ?subject <" + ZdoTerms.group.getURI() + "> \"" + ZdoGroup.ZDO.name() + "\"^^xsd:string.\n" +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.monograph.name() + "\"^^xsd:string. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.periodical.name() + "\"^^xsd:string. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.cho.name() + "\"^^xsd:string. } UNION " +
                "  { ?subject <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.bornDigital.name() + "\"^^xsd:string. }\n" +
                "  FILTER (\"" + from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\"^^xsd:dateTime <= ?modifiedTime && ?modifiedTime <= \"" + until.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\"^^xsd:dateTime).\n" +
                "} ORDER BY ?modifiedTime LIMIT " + (PAGE_SIZE + 1) + " OFFSET " + page*PAGE_SIZE + "\n";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);

        //Go through results and construct OAI-PMH headers for each
        ResultSet rs = queryExecution.execSelect();
        if(!rs.hasNext()) {
            throw new NoRecordsMatchPmhException();
        }
        int counter = 0;
        Map<String, String> resultMap = new HashMap<>();
        while(rs.hasNext()) {
            //We set that resumption token should be issued, and rest of the records will be returned next time
            if(++counter > PAGE_SIZE) {
                hasMore.set(true);
                break;
            }
            QuerySolution querySolution = rs.next();
            String lastModified = querySolution.getLiteral("modifiedTime").toString();
            lastModified = lastModified.substring(0, lastModified.indexOf("T"));
            resultMap.put(invIdToOaiId(querySolution.getLiteral("invId").getString()), lastModified);
        }
        return resultMap;
    }

    private String oaiIdToInvId(String oaiId) {
        if(oaiId.startsWith("oai:inqool.com:")) {
            return oaiId.substring("oai:inqool.com:".length());
        }
        else {
            throw new RuntimeException("Invalid OAI identifier format.");
        }
    }

    private String invIdToOaiId(String invId) {
        return "oai:inqool.com:" + invId;
    }
}
