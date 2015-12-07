package com.inqool.dcap.office.api;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.common.OrgToSuperKuratorMappingAccess;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.core.batch.BatchBasicOperations;
import com.inqool.dcap.office.api.core.batch.BatchDocAdder;
import com.inqool.dcap.office.api.core.batch.BatchDocDeleter;
import com.inqool.dcap.office.api.core.batch.BatchPublisher;
import com.inqool.dcap.office.api.util.OfficeException;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoOrganization;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Regularly runs OAI harvest.
 * @author Lukas Jane (inQool) 8. 6. 2015.
 */
@ApplicationScoped
@Path("/autopublish/")
public class AutoPublisher {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Inject
    private DataStore store;

    @Inject
    private PicketLinkAccess plAccess;

    @Inject
    private BatchBasicOperations batchBasicOperations;

    @Inject
    private BatchDocAdder batchDocAdder;

    @Inject
    private BatchPublisher batchPublisher;

    @Inject
    private OrgToSuperKuratorMappingAccess orgToSuperKuratorMappingAccess;

    @Inject
    private BatchDocDeleter batchDocDeleter;

    @Path("/start/")
    @POST
    public void startAutopublish() {
        autopublish();
    }

    public synchronized void autopublish() {    //definitely, there should NOT run two autopublishes at once
        logger.info("Starting autopublish of documents.");
        for (ZdoOrganization zdoOrganization : plAccess.listOrganizations()) {
            try {
                autopublish(zdoOrganization.getName());
            } catch (Exception e) {
                logger.error("Could not autopublish documents of org " + zdoOrganization.getName(), e);
            }
        }
    }

    private void autopublish(String orgName) throws IOException, OfficeException {
        logger.info("Running autopublish for organization " + orgName);
        String superKurator = orgToSuperKuratorMappingAccess.getOrgSuperKurator(orgName);
        if (superKurator == null) {
            throw new RuntimeException("This organization does not have a superKurator.");
        }
        do {
            List<String> docsToPublish = triplestoreStuff.findDocsThatShouldBePublished(orgName, 100);
            int batchId = 0;
            Iterator<String> iterator = docsToPublish.iterator();
            store.startTransaction();
            try {
                //Check if documents have all values required for publishing
                while (iterator.hasNext()) {
                    String docId = iterator.next();
                    ZdoModel kdrModel = store.get(store.createUrl(docId));
                    if (!isFilled(kdrModel)) {
                        iterator.remove();
                    }
                }

                if (docsToPublish.isEmpty()) {
                    store.rollbackTransaction();
                    return;
                }

                //Create batch
                logger.debug("Creating autopublish batch.");
                batchBasicOperations.setUser(superKurator);
                batchId = batchBasicOperations.createNewBatch("Automaticky publikovaná dávka.");

                //Create their concepts
                logger.debug("Adding autopublish concepts to batch.");
                batchDocAdder.setUser(superKurator);
                batchDocAdder.addDocuments(docsToPublish, batchId);
                store.commitTransaction();
            } catch (Exception e) {
                store.rollbackTransaction();
                if (batchId != 0) {
                    batchDocDeleter.setUser(superKurator);
                    batchDocDeleter.discardBatch(batchId);
                }
                throw new RuntimeException("Failed when trying to create concepts of documents in autopublish.", e);
            }

            try {
                //Publish them
                logger.debug("Publishing the autopublish batch.");
                store.startTransaction();
                batchPublisher.setUser(superKurator);
                batchPublisher.publishBatch(batchId);
                store.commitTransaction();
            } catch (Exception e) {
                store.rollbackTransaction();
                throw new RuntimeException("Failed when trying to publish documents in autopublish.", e);
            }
        } while(true);
    }

    private boolean isFilled(ZdoModel model) {
        String title = model.get(DCTerms.title);
        if (title == null || title.isEmpty()) {
            logger.warn("Document is missing a title: " + model.get(ZdoTerms.inventoryId));
            return false;
        }
        if(ZdoType.isRootCategory(model.get(ZdoTerms.zdoType))) {
            String docType = model.get(ZdoTerms.documentType);
            if (docType == null || docType.isEmpty()) {
                logger.warn("Document is missing a documentType: " + model.get(ZdoTerms.inventoryId));
                return false;
            }
            return true;
        }
        else {
            return isFilled(store.get(model.getParent()));
        }
    }
}
