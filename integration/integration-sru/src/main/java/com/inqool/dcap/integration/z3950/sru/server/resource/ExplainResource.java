package com.inqool.dcap.integration.z3950.sru.server.resource;

import com.inqool.dcap.integration.z3950.sru.server.config.IndexConversionMapping;
import com.inqool.dcap.integration.z3950.sru.server.config.SruDiagnosticsConstants;
import com.inqool.dcap.integration.z3950.sru.server.request.ExplainRequest;
import org.jboss.resteasy.annotations.Form;
import org.oasis_open.docs.ns.search_ws.diagnostic.DiagnosticComplexType;
import org.oasis_open.docs.ns.search_ws.sruresponse.*;
import org.z3950.explain.dtd._2.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
@RequestScoped
public class ExplainResource extends OperationResource {
    private static final String HOST = "zdo"; //todo set
    private static final String PORT = "80";
    private static final String DATABASE = "sru"; //should contain url path between host/ and ? mark

    private static final String TITLE = "DCAP -- Digital content access portal";
    private static final String DESCRIPTION = "SRU/Z39.50 Gateway to DCAP server.  Records in UTF-8 encoding.";

    @Inject
    private IndexConversionMapping indices;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public ExplainResponseDefinition handle(@Form ExplainRequest explainRequest) {
        ExplainResponseDefinition explainResponse = new ExplainResponseDefinition();
        explainResponse.setVersion("2.0");
        if(explainRequest.getVersion() != null) {
            if("1.1".equals(explainRequest.getVersion()) || "1.2".equals(explainRequest.getVersion())) {
                explainResponse.setVersion(explainRequest.getVersion());
            }
        }

        //Not allow stylesheets
        if(explainRequest.getStylesheet() != null) {
            addDiagnostics(explainResponse, SruDiagnosticsConstants.STYLESHEETS_NOT_SUPPORTED, null, "Stylesheets not supported.");
        }

        //Allow only xml packing
        if(explainRequest.getRecordPacking() != null && !"xml".equals(explainRequest.getRecordPacking())) {
            addDiagnostics(explainResponse, SruDiagnosticsConstants.UNSUPPORTED_PARAMETER_VALUE, "recordPacking", "Only xml record packing supported.");
            return explainResponse;
        }

        //The explain record itself
        RecordDefinition record = new RecordDefinition();
        record.setRecordSchema("http://explain.z3950.org/dtd/2.0/");
        record.setRecordXMLEscaping(RecordXMLEscapingDefinition.XML);
        record.setRecordData(createExplainRecordData());
        record.setRecordPosition(BigInteger.ONE);
        explainResponse.setRecord(record);
        return explainResponse;
    }

    /**
     * Creates explain record data
     * @return explain record data
     */
    private StringOrXmlFragmentDefinition createExplainRecordData() {
        Explain explain = new Explain();
        {   //Server info
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setProtocol("SRU");
            serverInfo.setTransport("http");
            serverInfo.setHost(HOST);
            serverInfo.setPort(PORT);
            serverInfo.setVersion("2.0");
            DatabaseDefinition databaseDefinition = new DatabaseDefinition();
            databaseDefinition.setValue(DATABASE);
            serverInfo.setDatabase(databaseDefinition);
            explain.setServerInfo(serverInfo);
        }
        {   //Database info
            DatabaseInfo databaseInfo = new DatabaseInfo();
            {   //Description
                StringPlusPrimaryPlusLang description = new StringPlusPrimaryPlusLang();
                description.setLang("en");
                description.setPrimary(TrueOrFalse.TRUE);
                description.setValue(DESCRIPTION);
                databaseInfo.getDescription().add(description);
            }
            {   //Title
                StringPlusPrimaryPlusLang title = new StringPlusPrimaryPlusLang();
                title.setLang("en");
                title.setPrimary(TrueOrFalse.TRUE);
                title.setValue(TITLE);
                databaseInfo.getTitle().add(title);
            }
            explain.setDatabaseInfo(databaseInfo);
        }
        {   //Index info
            IndexInfo indexInfo = new IndexInfo();
            List<Object> indexInfoList = indexInfo.getSetOrIndexOrSortKeyword();
            {   //Set
                SetDefinition setDefinition = new SetDefinition();
                setDefinition.setName("dc");
                setDefinition.setIdentifier("info:srw/cql-context-set/1/dc-v1.1");
                indexInfoList.add(setDefinition);
            }
            {   //Indices
                for(String set : indices.getMapping().keySet()) {
                    for(String index : indices.getMapping().get(set).keySet()) {
                        if(index == null || "".equals(index)) continue;
                        IndexDefinition indexDefinition = new IndexDefinition();
                        {   //Title
                            StringPlusPrimaryPlusLang title = new StringPlusPrimaryPlusLang();
                            title.setValue(index.substring(0, 1).toUpperCase() + index.substring(1)); //capitalize first letter
                            indexDefinition.getTitle().add(title);
                        }
                        {   //Map
                            MapDefinition map = new MapDefinition();
                            StringPlusSet name = new StringPlusSet();
                            name.setValue(index);
                            name.setSet(set);
                            map.setName(name);
                            indexDefinition.getMap().add(map);
                        }
                        indexInfoList.add(indexDefinition);
                    }
                }
            }
            explain.setIndexInfo(indexInfo);
        }
        {   //Schema info
            SchemaInfo schemaInfo = new SchemaInfo();
            {   //DC schema
                SchemaDefinition schemaDefinition = new SchemaDefinition();
                schemaDefinition.setName("dc");
                schemaDefinition.setIdentifier("info:srw/schema/1/dc-v1.1");
                {   //Title
                    StringPlusPrimaryPlusLang title = new StringPlusPrimaryPlusLang();
                    title.setValue("Dublin Core");
                    schemaDefinition.getTitle().add(title);
                }
                schemaInfo.getSchema().add(schemaDefinition);
            }
            explain.setSchemaInfo(schemaInfo);
        }
        StringOrXmlFragmentDefinition recordData = new StringOrXmlFragmentDefinition();
        recordData.getContent().add(explain);
        return recordData;
    }

    /**
     * Adds diagnostics entry to response
     * @param explainResponse
     * @param number error number
     * @param details
     * @param message
     */
    private void addDiagnostics(ExplainResponseDefinition explainResponse, int number, String details, String message) {
        DiagnosticsDefinition diagnostics = explainResponse.getDiagnostics();
        if(diagnostics == null) diagnostics = new DiagnosticsDefinition();
        {
            DiagnosticComplexType diagnostic = new DiagnosticComplexType();
            diagnostic.setUri(SruDiagnosticsConstants.DIAGNOSTIC_URI_PREFIX + number);
            diagnostic.setDetails(details);
            diagnostic.setMessage(message);
            diagnostics.getDiagnostic().add(diagnostic);
        }
        explainResponse.setDiagnostics(diagnostics);
    }
}
