/*
package com.inqool.dcap.office.indexer.config;

import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

*/
/**
 * Simplifies creation of Solr API interface
 *//*

@SuppressWarnings("unused")
@ApplicationScoped
public class Solr {
    @Inject
    @ConfigProperty(name = "solr.endpoint")
    private String solrEndpoint;

    @Produces
    @Zdo
    @ApplicationScoped
    public SolrServer solrServer() {
        return new HttpSolrServer(solrEndpoint);
    }
}
*/
