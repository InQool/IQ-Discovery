package com.inqool.dcap.integration.service;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemote;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.service.jena.ZdoUpdateProcessRemote;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.jena.atlas.io.IndentedWriter;
import org.slf4j.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Lukas Jane (inQool) 9. 4. 2015.
 */
@Dependent
@Stateless
public class SparqlOnDemandIndexer {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String queryBase;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String updateBase;

    @Asynchronous
    public Future<Boolean> asyncUpdatesAndDeletes(List<ZdoModel> toUpdate, List<String> toDelete) {
        try {
            bulkUpdate(toUpdate);
            bulkDelete(toDelete);
        } catch (Exception e) {
            return new AsyncResult<>(false);
        }
        return new AsyncResult<>(true);
    }

    public void bulkUpdate(List<ZdoModel> models) {
        logger.debug("Received triplestore bulk update request.");
        List<String> urlsToRemove = new ArrayList<>();
        models.forEach(model -> urlsToRemove.add(store.removeTransactionFromUrl(model.getUrl())));
        bulkDelete(urlsToRemove);

        final QuadDataAcc add = new QuadDataAcc();
        for (ZdoModel model : models) {
            // build a list of triples
            final StmtIterator triples = model.listStatements();    //todo maybe filter only right subjects?

            while ( triples.hasNext() ) {
                Statement s = triples.nextStatement();
                String subjectUri = store.removeTransactionFromUrl(s.getSubject().getURI());
                if(subjectUri.contains("fcr:metadata")
                        || subjectUri.contains("fcr:export?format=jcr/xml")
                        || subjectUri.equals("http://fedora.info/definitions/v4/repository#jcr/xml")) {
                    continue; //don't save useless metadata triples
                }
                String propertyUri = s.getPredicate().getURI();
                if(propertyUri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns")) continue;    //don't save rdf:type triplets
                if(propertyUri.startsWith("http://fedora.info/definitions/v4/repository#") && !(propertyUri.endsWith("created") || propertyUri.endsWith("lastModified") || propertyUri.endsWith("mimeType"))) continue; //skip fedora triplets other than created and lastModified
                RDFNode value = s.getObject();
/*            if(value.isResource()) {  //no longer needed
                value = new ResourceImpl(store.removeTransactionFromUrl(value.asResource().getURI()));
            }*/
                //We convert url-like strings to resources
                if(value.isLiteral() && value.asLiteral().getString().startsWith("http")) {
                    value = new ResourceImpl(value.asLiteral().getString());
                }
                Statement s2 = new StatementImpl(new ResourceImpl(subjectUri), s.getPredicate(), value);
                add.addTriple(s2.asTriple());
            }
        }
        // send update to server
        logger.debug("Sending triplestore update request.");
        exec(new UpdateRequest(new UpdateDataInsert(add)));
    }

    public void bulkDelete(List<String> urlsToRemove) {
        logger.debug("Received triplestore bulk delete request.");
        String deleteQuery = "DELETE { ?s ?p ?o. } WHERE { \n" +
                "  ?s ?p ?o.\n" +
                "  VALUES ?s {\n";

        for(String urlToRemove : urlsToRemove) {
            deleteQuery += "    <" + urlToRemove + ">\n";
        }
        deleteQuery += "  }.\n" +
                "}";

        final UpdateRequest del = new UpdateRequest();
        del.add(deleteQuery);

        UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(del, updateBase);
        updateProcessor.execute();
    }

    /**
     * Remove any current triples about the Fedora object and replace them with
     * the provided content.
     * content RDF in N3 format.
     **/
    public Future<Void> update(final ZdoModel model) {
        logger.debug("Received update for: {}", model.getUrl());
        removeSynch(store.removeTransactionFromUrl(model.getUrl()));
        // build a list of triples
        final StmtIterator triples = model.listStatements();    //todo maybe filter only right subjects?
        final QuadDataAcc add = new QuadDataAcc();
        while ( triples.hasNext() ) {
            Statement s = triples.nextStatement();

            String subjectUri = store.removeTransactionFromUrl(s.getSubject().getURI());
            if(subjectUri.contains("fcr:metadata") || subjectUri.contains("fcr:export?format=jcr/xml")) continue; //don't save useless metadata triples
            String propertyUri = s.getPredicate().getURI();
            if(propertyUri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns")) continue;    //don't save rdf:type triplets
            if(propertyUri.startsWith("http://fedora.info/definitions/v4/repository#") && !(propertyUri.endsWith("created") || propertyUri.endsWith("lastModified"))) continue; //skip fedora triplets other than created and lastModified
            RDFNode value = s.getObject();
/*            if(value.isResource()) {  //no longer needed
                value = new ResourceImpl(store.removeTransactionFromUrl(value.asResource().getURI()));
            }*/
            //We convert url-like strings to resources
            if(value.isLiteral() && value.asLiteral().getString().startsWith("http")) {
                value = new ResourceImpl(value.asLiteral().getString());
            }
            Statement s2 = new StatementImpl(new ResourceImpl(subjectUri), s.getPredicate(), value);
            add.addTriple(s2.asTriple());
        }

        // send update to server
        logger.debug("Sending update request for url: {}", model.getUrl());
        exec(new UpdateRequest(new UpdateDataInsert(add)));
        return new AsyncResult<>(null);
    }

    public Future<Void> remove(final String subject) {
        logger.debug("Received remove for: {}", subject);
        removeSynch(subject);
        return new AsyncResult<>(null);
    }

    /**
     * Perform a DESCRIBE query for triples about the Fedora object and remove
     * all triples with subjects starting with the same subject.
     **/
    private void removeSynch(final String subject) {
        // find triples/quads to delete
        final String describeQuery = "DESCRIBE <" + subject + ">";
        final QueryEngineHTTP qexec = new QueryEngineHTTP(queryBase, describeQuery );
        final Iterator<Triple> results = qexec.execDescribeTriples();

        // build list of triples to delete
        final Set<String> uris = new HashSet<>();
        while ( results.hasNext() ) {
            final Triple triple = results.next();

            // add subject uri, if it is part of this object
            if ( triple.getSubject().isURI() ) {
                final String uri = triple.getSubject().getURI();
                if ( matches(subject, uri) ) {
                    uris.add(uri);
                }
            }

            // add object uri, if it is part of this object
            if ( triple.getObject().isURI() ) {
                final String uri = triple.getObject().getURI();
                if ( matches(subject, uri) ) {
                    uris.add(uri);
                }
            }
        }
        qexec.close();

        // build update commands
        final UpdateRequest del = new UpdateRequest();
        for (final String uri : uris) {
            final String cmd = "DELETE WHERE { <" + uri + "> ?p ?o }";
            logger.debug("Executing: {}", cmd);
            del.add(cmd);
        }

        // send updates
        exec(del);
    }

    /**
     * Determine whether arg candidate is a sub-URI of arg resource, defined as candidate-URI starting
     * with resource-URI, plus an option suffix starting with a hash (#) or slash (/)
     * suffix.
     **/
    private boolean matches( final String resource, final String candidate) {
        // All triples that will match this logic are ones that:
        // - have a candidate subject or object that equals the target resource of removal, or
        // - have a candidate subject or object that is prefixed with the resource of removal
        //    (therefore catching all children).
        return resource.equals(candidate) || candidate.startsWith(resource + "/")
                || candidate.startsWith(resource + "#");
    }

    private void exec(final UpdateRequest update) {
        if (update.getOperations().isEmpty()) {
            logger.debug("Received empty update/remove operation.");
            return;
        }

        // execute SPARQL update
        final UpdateProcessRemote proc = new ZdoUpdateProcessRemote(update, updateBase, Context.emptyContext);
        try {
            proc.execute();
        } catch (final Exception e) {
            logger.error("Error executing Sparql update/remove!", e);
        }

        logger.debug("Completed Sparql update/removal.");
        if (logger.isTraceEnabled()) {
            try (final OutputStream buffer = new ByteArrayOutputStream()) {
                final IndentedWriter out = new IndentedWriter(buffer);
                update.output(out);
                logger.trace("Executed update/remove operation:\n{}", buffer.toString());
                out.close();
            } catch (final IOException e) {
                logger.error("Couldn't retrieve execution of update/remove operation!", e);
            }
        }
    }
}
