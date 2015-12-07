/*
 * DataStore.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.inqool.dcap.integration.service;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RequestScoped
public class DataStore {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "fedora.endpoint")
    private String FEDORA_ENDPOINT;

    @Inject
    private SparqlOnDemandIndexer sparqlOnDemandIndexer;

    //Delayed operations on triplestore
    private List<ZdoModel> toUpdate = new ArrayList<>();
    private List<String> toDelete = new ArrayList<>();

    private final String RDF_SERIALIZATION = "text/rdf+n3";

    /*
    Transaction usage:
    - optional - if transaction is not started, api functions work without transaction
    - call startTransaction(), then use other api functions, and finally do commitTransaction() or rollbackTransaction()
    GET methods are not in transaction so far ... needed?
     */
    private String transactionPath;

    public ZdoModel get(String url) {
        Response response = ClientBuilder.newClient()
                .target(fitTransactionToUrl(url))
                .request()
                .accept(RDF_SERIALIZATION)
                .get();

        if(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            logger.error("Get on Fedora url " + url + " returned " + response.getStatus() + ": " + response.readEntity(String.class));
            return null;
        }

        if (response.getMediaType().toString().equals("text/rdf+n3")) {
            ZdoModel result = response.readEntity(ZdoModel.class);
            if(result == null) return null;
            result.setUrl(fitTransactionToUrl(url));    //sometimes fedora returns url without transaction in url, but subjects with it...then get property doesn't work of course
            result.stripPossibleBadUrlEnding();  //sometimes we get unwanted query parameters or such, but we need url to exactly match the RDF subject
            return result;
        } else {
            logger.debug("Tried to retrieve non RDF resource '{}', retreiving from /fcr:metadata node instead.", url);
            ZdoModel model = get(url + "/fcr:metadata");
            model.setUrl(fitTransactionToUrl(url));
            return model;
        }
    }

    public void update(ZdoModel model) throws IOException {
        if (model.getContent() == null) {
            updateMetadata(model);
        } else {
            updateBinary(model);
        }
    }

    private void updateBinary(ZdoModel model) throws IOException {
        try (InputStream in = model.getContent()) {
            Response.StatusType statusInfo = ClientBuilder.newClient()
                    .target(fitTransactionToUrl(model.getUrl()))
                    .request()
                    .put(Entity.entity(in, model.get(ZdoTerms.mimeType)))
                    .getStatusInfo();

            if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(model.getUrl(), statusInfo);
                throw new IOException("Failed to update resource " + model.getUrl());
            } else {
                model.setUrl(model.getUrl() + "/fcr:metadata");
                patchMetadata(model);
                model.setUrl(model.getUrl().substring(0, model.getUrl().length() - "/fcr:metadata".length()));

                //Also update triplestore
                model.stripPossibleBadUrlEnding();   //strip fcr:metadata
                if(transactionPath == null) {
                    sparqlOnDemandIndexer.update(model);
                }
                else {  //If in transaction, triplestore operations are delayed till its end
                    toUpdate.add(model);
                }
            }
        }
    }

    private void updateMetadata(ZdoModel model) throws IOException {
        Response.StatusType statusInfo = ClientBuilder.newClient()
                .target(fitTransactionToUrl(model.getUrl()))
                .request()
                .put(Entity.entity(model, RDF_SERIALIZATION))
                .getStatusInfo();

        if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
            logError(fitTransactionToUrl(model.getUrl()), statusInfo);
            throw new IOException("Failed to update resource " + fitTransactionToUrl(model.getUrl()));
        }

        model.removeAllValuesOfProperty(ZdoTerms.fedoraLastModified);
        if(ZdoType.isAbovePageCategory(model.get(ZdoTerms.zdoType))) {
            model.add(model.getSubject(), ZdoTerms.fedoraLastModified, OffsetDateTime.now().toString(), XSDDatatype.XSDdateTime);
            if(model.get(ZdoTerms.fedoraCreated) == null) {
                model.add(model.getSubject(), ZdoTerms.fedoraCreated, OffsetDateTime.now().toString(), XSDDatatype.XSDdateTime);
            }
        }
        else {
            model.removeAllValuesOfProperty(ZdoTerms.fedoraCreated);
        }

        //Also update triplestore
        model.stripPossibleBadUrlEnding();   //strip fcr:metadata
        if(transactionPath == null) {
            sparqlOnDemandIndexer.update(model);
        }
        else {  //If in transaction, triplestore operations are delayed till its end
            toUpdate.add(model);
        }
    }

    public void patchMetadata(ZdoModel model) {
        //Warning: this does not go to triplestore
        //Warning: this will work only for this use case but fail miserably if used for something else
        /*  We tried to update fedora with the updateMetadata() method, but it fails with 400.
        This happens only when using transaction and trying to update metadata node that was just inserted.
        Theory is, that fedora does not realize this operation should be on temporary transaction node
        and tries to perform update on non-transaction node (which does not exist until transaction is committed.
        Hence, we use this sparql workaround that does not have this error.
        */
        String updateString = "INSERT {   \n";
        StmtIterator iter = model.listStatements();
        if(!iter.hasNext()) {
            return; //If there is nothing to update, return
        }
        while(iter.hasNext()) {
            Statement statement = iter.next();
            String subject = statement.getSubject().getURI();
            String predicate = statement.getPredicate().getURI();
            if(!predicate.startsWith("http://inqool.cz/zdo/") && !predicate.startsWith(DCTerms.NS)) {
                continue;
            }
            String value = statement.getObject().asLiteral().getString();

            String updateLine = " <" + fitTransactionToUrl(subject) + "> <" + predicate + "> \"" + value + "\".\n";
            updateString += updateLine;
        }
        updateString += "}\nWHERE { }";

        Response response = ClientBuilder.newClient()
                .target(fitTransactionToUrl(model.getUrl()))
                .request()
                .method("PATCH", Entity.entity(updateString, "application/sparql-update"));

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            logError(model.getUrl(), response.getStatusInfo());
            throw new RuntimeException("Failed to update resource " + model.getUrl());
        }
    }

    public void delete(String url) {
        Response.StatusType statusInfo = ClientBuilder.newClient()
                .target(fitTransactionToUrl(url))
                .request()
                .delete()
                .getStatusInfo();

        if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
            logError(fitTransactionToUrl(url), statusInfo);
            //Delete operations mostly fail because the item already is not there - which can be considered a desired state, so no exception
            /*throw new IOException("Failed to update resource " + fitTransactionToUrl(url));*/
        }

        //Also update triplestore
        if(transactionPath == null) {
            sparqlOnDemandIndexer.remove(removeTransactionFromUrl(url));
        }
        else {  //If in transaction, triplestore operations are delayed till its end
            toDelete.add(removeTransactionFromUrl(url));
        }
    }

    private void logError(String url, Response.StatusType statusInfo) {
        logger.error("Failed to complete operation on {}. Error message is ({}) : {}.",
                url, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
    }

    public String createUrl(String id) {
        return FEDORA_ENDPOINT + createDeepPath(id);
    }

    public String createDeepPath(String id) {
        return id.substring(0, 2) + "/" + id.substring(2, 4) + "/" + id.substring(4, 6) + "/" + id.substring(6, 8) + "/" + id;
    }

    //When saving fedora urls somewhere, transactions must be cut out because that url is not valid after end of transaction
    public String removeTransactionFromUrl(String url) {
        if(url == null) return null;
        int txPos = url.indexOf("/tx:");
        if(txPos == -1) return url;
        int txEnd = url.indexOf("/", txPos+1);
        return url.substring(0, txPos) + url.substring(txEnd);
    }

    public String getOnlyIdFromUrl(String url) {
        if(url.contains("?")) { //remove query parameters
            url = url.substring(0, url.indexOf("?"));
        }
        if((url.length() - url.lastIndexOf("/")) < 20 ) {   //if url does not end with UUID, strip the foreign last part
            url = url.substring(0, url.lastIndexOf("/"));
        }
        return url.substring(url.lastIndexOf("/")+1);
    }

    private String fitTransactionToUrl(String url) {
        if(transactionPath == null) {
            return url;
        }
        if(url.contains("/tx:")) {  //Fedora tries to be too smart and fills transactions to fields of the model that is received by transaction, if that happened, don't add transaction again
            return url;
        }
        int restPos = url.lastIndexOf("/rest/");
        if(restPos == -1) {
            throw new RuntimeException("Word rest not found in url.");
        }
        return transactionPath + url.substring(restPos + 5);

    }

    public void startTransaction() {
        if(transactionPath != null) {
            throw new RuntimeException("Fedora transaction already active, can't start new one.");
        }

        Response response = ClientBuilder.newClient()
                .target(FEDORA_ENDPOINT + "fcr:tx")
                .request()
                .post(null);
        if(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException("Fedora responded with error when starting transaction.");
        }

        URI txLocation = response.getLocation();
        if(txLocation == null) {
            throw new RuntimeException("There was no transaction info in fedora's response.");
        }
        toUpdate.clear();
        toDelete.clear();
        transactionPath = txLocation.toString();
    }

    public void commitTransaction() {
        try {
            if (transactionPath == null) {
                throw new RuntimeException("No fedora transaction active, can't commit.");
            }
            if (!(toUpdate.isEmpty() && toDelete.isEmpty())) {
                Response response = ClientBuilder.newClient()
                        .target(transactionPath + "/fcr:tx/fcr:commit")
                        .request()
                        .post(null);
                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    throw new RuntimeException("Fedora responded with error when commiting transaction. " + response.readEntity(String.class));
                }
                releaseTheHounds();
            }
        }
        finally {
            transactionPath = null;
        }
    }

    public void rollbackTransaction() {
        try {
            if (transactionPath == null) {
                logger.warn("No fedora transaction active, can't rollback.");
                return; //Better not to throw exception here, the transaction probably was rolled back in other way or never started, so the purpose of voiding the transaction is reached anyway
            }
            toUpdate.clear();
            toDelete.clear();

            Response response = ClientBuilder.newClient()
                    .target(transactionPath + "/fcr:tx/fcr:rollback")
                    .request()
                    .post(null);
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("Fedora responded with error when trying to rollback transaction. " + response.readEntity(String.class));
            }
        }
        finally {
            transactionPath = null;
        }
    }

    private void releaseTheHounds() {
        Future<Boolean> future = sparqlOnDemandIndexer.asyncUpdatesAndDeletes(toUpdate, toDelete);
        boolean success = false;
        try {
            success = future.get(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Async triplestore update exception", e);
        }
        if(!success) {
            logger.error("Triplestore update timed out, retrying.");
            future = sparqlOnDemandIndexer.asyncUpdatesAndDeletes(toUpdate, toDelete);
            try {
                success = future.get(120, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Async triplestore retry update exception", e);
            }
            if(!success) {
                toUpdate.clear();
                toDelete.clear();
                throw new RuntimeException("Triplestore update timed out twice, that sux a big one.");
            }
        }
        toUpdate.clear();
        toDelete.clear();
    }

    //Debug function, gets totally all objects from Fedora
    public List<String> getAll() {
        ZdoModel model = get(FEDORA_ENDPOINT + "/");
        return model.getAll(new PropertyImpl("http://www.w3.org/ns/ldp#contains"));
    }
}
