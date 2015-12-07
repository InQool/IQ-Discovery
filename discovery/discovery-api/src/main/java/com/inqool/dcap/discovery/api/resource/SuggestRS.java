package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 23. 2. 2015.
 */
@RequestScoped
@Path("/suggest")
public class SuggestRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "solr.endpoint.suggester")
    private String SOLR_SUGGESTER_ENDPOINT;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response search(@QueryParam("query") String query) throws SolrServerException {
        //Query the Solr
        SolrServer server = new HttpSolrServer(SOLR_SUGGESTER_ENDPOINT);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        solrQuery.setRequestHandler("/suggest");

        QueryResponse rsp = server.query(solrQuery);

        List<String> resultList = new ArrayList<>();
        //Please don't ask
        //Ok you asked: SolrJ does not support search hints... well with this line it does... hopefully...
        ((List) ((SimpleOrderedMap) ((NamedList) ((Map) rsp.getResponse().get("suggest")).get("mySuggester")).getVal(0)).get("suggestions")).forEach((x -> resultList.add((String)((NamedList) x).get("term"))));
        return Response.ok(resultList).build();
    }
}
