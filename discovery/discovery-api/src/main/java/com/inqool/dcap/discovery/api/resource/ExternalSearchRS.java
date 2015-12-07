package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.common.entity.SruSource;
import com.inqool.dcap.common.entity.Z3950Source;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.ExternalSourcesAccess;
import com.inqool.dcap.integration.SruClient;
import com.inqool.dcap.integration.z3950.client.Searcher;
import com.inqool.dcap.integration.Utils;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.openarchives.oai._2_0.oai_dc.DcCollection;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Lukas Jane (inQool) 23. 2. 2015.
 */
@RequestScoped
@Path("/search/external/")
public class ExternalSearchRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private ExternalSourcesAccess externalSourcesAccess;

    @Inject
    private SruClient sruClient;

    @Inject
    @ConfigProperty(name = "solr.endpoint.external")
    private String SOLR_EXTERNAL_SOURCES_ENDPOINT;

    @GET
    @Path("/z3950/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response listZ3950sources() {
        return Response.ok(externalSourcesAccess.listZ3950Sources()).build();
    }

    @GET
    @Path("/z3950/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response searchZ3950(@PathParam("id") int id, @QueryParam("query") String query) throws IRResultSetException, SearchException, MalformedURLException {
        Z3950Source z3950Source = externalSourcesAccess.getZ3950Source(id);

        Searcher searcher = new Searcher();
        String result;
/*
        searcher.init("localhost", 9999, "Default", "UTF-8");
        String result = searcher.search("dc.title = bitch");
        System.out.println(result);*/

/*        searcher.init("aleph.mzk.cz", 9991, "MZK01-UTF", "UTF-8");
        //String result = searcher.search("@attrset bib-1 @attr 1=4 \"utah\"");
        result = searcher.search("dc.title = morava");*/

        URL urlParsed = new URL(z3950Source.getUrl());
        searcher.init(urlParsed.getHost(), urlParsed.getPort(), z3950Source.getDatabaseName(), "CP1250");
        //String result = searcher.search("@attrset bib-1 @attr 1=4 \"utah\"");
        result = searcher.search(query);

        System.out.println(result);

        String dcXmlCollection = Utils.marcXmlToDcCollectionXml(result);

        DcCollection dcCollection = Utils.unmarshallDcCollectionXml(dcXmlCollection);

        return Response.ok(Utils.dcCollectionToJsonSerializable(dcCollection)).build();
    }

    @GET
    @Path("/sru/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response listSruSources() {
        return Response.ok(externalSourcesAccess.listSruSources()).build();
    }

    @GET
    @Path("/sru/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response searchSru(@PathParam("id") int id, @QueryParam("query") String query) throws IRResultSetException, SearchException, MalformedURLException {
        SruSource sruSource = externalSourcesAccess.getSruSource(id);

        if (!sruSource.getUrl().endsWith("/")) {
            sruSource.setUrl(sruSource.getUrl() + "/");
        }

        DcCollection dcCollection = sruClient.performSru(sruSource.getUrl() + sruSource.getDatabaseName(), query);

//        System.out.println(result);
//
//        String dcXmlCollection = Utils.marcXmlToDcCollectionXml(result);
//
//        DcCollection dcCollection = Utils.unmarshallDcCollectionXml(dcXmlCollection);

        return Response.ok(Utils.dcCollectionToJsonSerializable(dcCollection)).build();
    }

    @GET
    @Path("/oai/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response listOaiSources() {
        return Response.ok(externalSourcesAccess.listOaiSources()).build();
    }

    @GET
    @Path("/oai/all/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response searchOai(@QueryParam("query") String query) {
        try {
            SolrServer server = new HttpSolrServer(SOLR_EXTERNAL_SOURCES_ENDPOINT);
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            QueryResponse response = server.query(solrQuery);

            SolrDocumentList results = response.getResults();

            return Response.ok(results).build();
        } catch (Exception e) {
            logger.error("Search in external oai solr failed.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
