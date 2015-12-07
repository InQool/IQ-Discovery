package com.inqool.dcap.discovery.api.resource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Lukas Jane (inQool)
 */
@Path("/data/ocr")
@RequestScoped
public class OcrResource {
    @Inject
    private DataStore dataStore;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Path("/{invId}/{pageId}/")
    @GET
    @Produces("text/plain")
    public Response getOcrPage(@PathParam("invId") String invId, @PathParam("pageId") String pageId) throws IOException {
        try {
            String queryString = "SELECT ?txt WHERE {\n" +
                    "?document <" + ZdoTerms.inventoryId.getURI() + "> " + ZdoTerms.stringConstantOf(invId) + ".\n" +
                    //First select the page under given document
                    "?page <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.page.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                    "?page <http://purl.org/dc/terms/isPartOf> ?document.\n" +
                    "?page <" + ZdoTerms.pageIndex.getURI() + "> \"" + pageId + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                    //then select its ocr text binary child and find its id
                    "?txt <http://purl.org/dc/terms/isPartOf> ?page.\n" +
                    "?txt <" + ZdoTerms.zdoType.getURI() + "> \"" + ZdoType.binary.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                    "?txt <" + ZdoTerms.fileType.getURI() + "> \"" + ZdoFileType.txt.name() + "\"^^<http://www.w3.org/2001/XMLSchema#string>.\n" +
                    "}\n";
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
            ResultSet resultSet = queryExecution.execSelect();
            if (!resultSet.hasNext()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            QuerySolution querySolution = resultSet.next();
            String txtUrl = querySolution.getResource("txt").getURI();
            InputStream in = new URL(txtUrl).openStream();
            return Response.ok(in).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
