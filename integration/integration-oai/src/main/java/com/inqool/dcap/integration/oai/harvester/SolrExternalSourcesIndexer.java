/**
 * Copyright 2014 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inqool.dcap.integration.oai.harvester;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.inqool.dcap.DCTools;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;

import javax.ejb.AsyncResult;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * A Solr Indexer implementation that adds information to a
 * Solr index server.
 *
 * @author ajs6f
 * @author yecao
 * @author Matus Zamborsky (inQool)
 * @author Lukas Jane (inQool) 19. 3. 2015.
 */
@RequestScoped
public class SolrExternalSourcesIndexer {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "solr.endpoint.external")
    private String SOLR_EXTERNAL_SOURCES_ENDPOINT;

    // TODO make index-time boost somehow adjustable, or something
    private static final Long INDEX_TIME_BOOST = 1L;

    public void update(ZdoModel model) throws IOException {
        SolrServer server = new HttpSolrServer(SOLR_EXTERNAL_SOURCES_ENDPOINT);

        String docId = model.get(ZdoTerms.oaiIdentifier);
        //First remove old doc
        try {
            final UpdateResponse resp = server.deleteById(docId);
            if (resp.getStatus() == 0) {
                logger.trace("Remove request was successful for: {}", docId);
            } else {
                logger.error("Remove request has error, code: {} for pid: {}", resp.getStatus(), docId);
                return;
            }
        } catch (final SolrServerException | IOException e) {
            logger.error("Delete Exception: {}", e);
            throw new RuntimeException(e);
        }

        //Recursively dig to fedora and triplestore to construct whole solr document hierarchy
        SolrInputDocument solrInputDocument = recursivelyIndex(model);
        logger.trace("Created SolrInputDocument: {}", solrInputDocument);

        if(solrInputDocument == null) return;   //this was probably a delete request, skip inserting

        //Then insert new docs
        try {
            logger.debug("Executing update of: {}...", store.removeTransactionFromUrl(model.getUrl()));
            final UpdateResponse resp = server.add(solrInputDocument);
            if (resp.getStatus() == 0) {
                logger.trace("Update request was successful for: {}", docId);
            } else {
                logger.error("Update request returned error code: {} for identifier: {}", resp.getStatus(), docId);
            }
            logger.trace("Received result from Solr request.");
        } catch (final SolrServerException | IOException e) {
            logger.error("Update exception: {}!", e);
            throw new RuntimeException(e);
        }
    }

    private SolrInputDocument recursivelyIndex(final ZdoModel model) throws IOException {
        if (model == null) {
            return null;
        }

        logger.debug("Resource: {} retrieved with indexable type.", store.removeTransactionFromUrl(model.getUrl()));

        if(!ZdoGroup.EXTERNAL.name().equals(model.get(ZdoTerms.group))) {
            logger.info("Not indexing this document as it is not external.");
            return null;
        }

        final SolrInputDocument inputDoc = modelToSolrInputDoc(model);

        inputDoc.addField("datePublished", LocalDateTime.now().atZone(ZoneOffset.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));  //solr needs UTC time

        return inputDoc;
    }

    protected SolrInputDocument modelToSolrInputDoc(ZdoModel model) {
        logger.debug("Constructing new SolrInputDocument...");

        final Map<String, SolrInputField> fields = new HashMap<>();

        //Add all Dublin Core terms
        for(String property : DCTools.getDcTermList()) {
            SolrInputField field = new SolrInputField(property);
            List<String> values = model.getAll(new PropertyImpl("http://purl.org/dc/terms/" + property));
            if(values.isEmpty()) continue;

            field.addValue(values, INDEX_TIME_BOOST);
            fields.put(property, field);
        }

        //Add system fields
        SolrInputField field = new SolrInputField("id");
        field.addValue(model.get(ZdoTerms.oaiIdentifier), INDEX_TIME_BOOST);
        fields.put("id", field);

        addSolrFieldFromFedoraProperty("inventoryId", ZdoTerms.inventoryId, model, fields);
        addSolrFieldFromFedoraProperty("source", ZdoTerms.source, model, fields);

        addSolrFieldFromFedoraProperty("zdoGroup", ZdoTerms.group, model, fields);

        return new SolrInputDocument(fields);
    }

    private void addSolrFieldFromFedoraProperty(String fieldName, Property property, ZdoModel model, Map<String, SolrInputField> fields) {
        addSolrField(fieldName, model.get(property), fields);
    }

    private void addSolrField(String fieldName, Object value, Map<String, SolrInputField> fields) {
        if(value == null) return;
        SolrInputField zdoTypeField = new SolrInputField(fieldName);
        zdoTypeField.addValue(value, INDEX_TIME_BOOST );
        fields.put(fieldName, zdoTypeField);
    }

    public Future<UpdateResponse> remove(final String id) {
        logger.debug("Received request for removal of: {}", id);
        try {
            SolrServer server = new HttpSolrServer(SOLR_EXTERNAL_SOURCES_ENDPOINT);

            final UpdateResponse resp = server.deleteById(id);
            if (resp.getStatus() == 0) {
                logger.debug("Remove request was successful for: {}", id);
            } else {
                logger.error("Remove request has error, code: {} for pid: {}", resp.getStatus(), id);
            }
            return new AsyncResult<>(resp);
        } catch (final SolrServerException | IOException e) {
            logger.error("Delete Exception: {}", e);
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        SolrServer server = new HttpSolrServer(SOLR_EXTERNAL_SOURCES_ENDPOINT);
        logger.debug("Commiting.");
        try {
            server.commit();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException("Solr commit failed.", e);
        }
    }
}
