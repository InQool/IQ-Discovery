package com.inqool.dcap.office.api.core.batch;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.TriplestoreStuff;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.security.model.ZdoUser;
import org.picketlink.Identity;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 25. 3. 2015.
 */
@RequestScoped
public class BatchDocDeleter {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Inject
    private BatchBasicOperations batchBasicOperations;

    private List<String> alreadyDiscardedSkipList = new ArrayList<>();

    @Inject
    private Identity identity;

    private String userUnderWhichToDiscardBatch;

    public void setUser(String zdoUser) {
        userUnderWhichToDiscardBatch = zdoUser;
    }

    //Just changing state to discarded

    @Transactional
    public void discardBatch(int batchId) throws IOException {
        if(userUnderWhichToDiscardBatch == null) {
            userUnderWhichToDiscardBatch = ((ZdoUser) identity.getAccount()).getLoginName();
        }

        ZdoBatch batch = em.find(ZdoBatch.class, batchId);

        if(batch == null || batch.isDeleted() || !batch.getOwner().equals(userUnderWhichToDiscardBatch)) {
            throw new RuntimeException("Bad batch id to discard.");
        }
        if(ZdoBatch.BatchState.unfinished != batch.getState()) {
            throw new RuntimeException("Cant discard discarded or published batch.");
        }
        batch.setState(ZdoBatch.BatchState.discarded);
        batch.setModified(LocalDateTime.now());
        for (String documentId : batch.getDocuments()) {
            discardDocIncludingAncestors(store.createUrl(documentId));
        }
    }

    @Transactional
    public void discardBatches(List<Integer> batchIds) throws IOException {
        for(int batchId : batchIds) {
            discardBatch(batchId);
        }
    }

    private void discardDocIncludingAncestors(String documentUrl) throws IOException {
        //Don't discard the same doc twice
        if(alreadyDiscardedSkipList.contains(store.getOnlyIdFromUrl(documentUrl))) {
            return;
        }

        ZdoModel model = store.get(documentUrl);
        if(model != null) {
            model.replaceValueOfProperty(ZdoTerms.group, ZdoGroup.DISCARDED.name());
            model.removeAllValuesOfProperty(ZdoTerms.owner);
            store.update(model);

            alreadyDiscardedSkipList.add(store.getOnlyIdFromUrl(model.getUrl()));

            //Unlock kdr document
            ZdoModel kdrModel = store.get(model.get(ZdoTerms.kdrObject));
            kdrModel.decreaseLockCount();
            store.update(kdrModel);

            String parentUrl = model.get(DCTerms.isPartOf);
            if(parentUrl != null) {
                discardDocIncludingAncestors(parentUrl);
            }
            //Actually, unpublished documents are not in solr yet and published should not be deleted
/*            else {  //this is root, unindex it from solr
                if(ZdoGroup.ZDO.name().equals(model.get(ZdoTerms.group))) {    //only published docs have been indexed
                    indexer.remove(store.removeTransactionFromUrl(documentUrl));
                }
            }*/
        }
    }

    //Complete deletion

    @Transactional
    public void deleteDocuments(List<String> docIds, int batchId) throws IOException {
        ZdoBatch batch = em.find(ZdoBatch.class, batchId);

        if(batch == null || batch.isDeleted() || !batch.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            throw new RuntimeException("Bad batch id.");
        }
        if(ZdoBatch.BatchState.unfinished != batch.getState()) {
            throw new RuntimeException("Cant discard documents from published batch.");
        }

        //Get urls of documents in batch to be able to tell if the documents requested to be deleted actually are in the batch
        List<String> batchDocumentIds = batch.getDocuments();
        /* Stores URLs of parents whose children were removed, and count of children removed
        used because in the end, we also need to remove parents who lost all their children */
        Map<String, Integer> parentRemovedChildCountMap = new HashMap<>();

        //Remove the documents from batch
        for (String docId : docIds) {
            if(batchDocumentIds.contains(docId)) {
                deleteDocument(store.createUrl(docId), parentRemovedChildCountMap);
                batchDocumentIds.remove(docId);
                batch.setModified(LocalDateTime.now());
            }
        }
        if(!parentRemovedChildCountMap.isEmpty()) {
            //Some parent gotten their children removed, they might be removed too if these were their last children
            maybeRemoveParents(parentRemovedChildCountMap);
        }
    }

    private void maybeRemoveParents(Map<String, Integer> currentParentRemovedChildCountMap) throws IOException {
        Map<String, Integer> newParentRemovedChildCountMap = new HashMap<>();

        for(String modelUrl : currentParentRemovedChildCountMap.keySet()) {
            if(triplestoreStuff.countDocChildren(modelUrl) == currentParentRemovedChildCountMap.get(modelUrl)) {
            /* if(triplestoreStuff.countDocChildren(modelUrl) == 0) { //this one was used before fedora transactions were cool */
                deleteDocument(modelUrl, newParentRemovedChildCountMap);
            }
        }
        //Recursively cull grandparents too
        if(!newParentRemovedChildCountMap.isEmpty()) {
            maybeRemoveParents(newParentRemovedChildCountMap);
        }
    }

    private void deleteDocument(String docUrl, Map<String, Integer> parentRemovedChildCountMap) throws IOException {
        ZdoModel model = store.get(docUrl);
        if(model == null) {
            throw new RuntimeException("Document specified could not be retrieved from Fedora.");
        }

        //If we are not root of hierarchy, remember that we deleted a child off our parent
        String parentUrl = model.get(DCTerms.isPartOf);
        if(parentUrl != null) {
            if (parentRemovedChildCountMap.containsKey(parentUrl)) {
                parentRemovedChildCountMap.put(parentUrl, parentRemovedChildCountMap.get(parentUrl) + 1);
            } else {
                parentRemovedChildCountMap.put(parentUrl, 1);
            }
        }
        //Unpublished docs are not in solr, no need to unindex it
/*        else {  //this is root, unindex it from solr
            if(ZdoGroup.ZDO.name().equals(model.get(ZdoTerms.group))) {    //only published docs have been indexed
                indexer.remove(store.removeTransactionFromUrl(model.getUrl()));
            }
        }*/
        store.delete(docUrl);

        //Unlock kdr object
        ZdoModel kdrModel = store.get(model.get(ZdoTerms.kdrObject));
        kdrModel.decreaseLockCount();
        store.update(kdrModel);
    }

    //Unpublishing

    @Transactional
    public List<String> unpublishDocument(String docId, int batchId) throws IOException {
        ZdoBatch batch = em.find(ZdoBatch.class, batchId);

        if(batch == null || batch.isDeleted() || !batch.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            throw new RuntimeException("Bad batch id.");
        }
        if(ZdoBatch.BatchState.published != batch.getState()) {
            throw new RuntimeException("Cant unpublish document from batch that isn't published.");
        }

        ZdoModel model = store.get(store.createUrl(docId));
        if(model == null || !Integer.valueOf(model.get(ZdoTerms.batchId)).equals(batchId)) {
            throw new RuntimeException("Document does not belong to the specified batch.");
        }

        //Become unpublished
        List<String> rootsToReindex = new ArrayList<>();
        unpublishDocumentInner(model, false, rootsToReindex);

        //Remove from batch
        /*batch.getDocuments().remove(docId);*/ //or don't
        batch.setModified(LocalDateTime.now());

        return rootsToReindex;
    }

    private void unpublishDocumentInner(ZdoModel model, boolean callerRemovedChild, List<String> rootsToReindex) throws IOException {
        if(model == null) {
            throw new RuntimeException("Document specified could not be retrieved from Fedora.");
        }

        boolean didRemoveChild = false;
        if(ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) {
            model.replaceValueOfProperty(ZdoTerms.group, ZdoGroup.UNPUBLISHED.name());
            model.removeAllValuesOfProperty(ZdoTerms.owner);
            store.update(model);
            didRemoveChild = true;

            //Unlock kdr object
            ZdoModel kdrModel = store.get(model.get(ZdoTerms.kdrObject));
            kdrModel.decreaseLockCount();
            String pdfUrl = kdrModel.get(ZdoTerms.pdfUrl);
            String epubUrl = kdrModel.get(ZdoTerms.epubUrl);
            if(pdfUrl != null) {
                store.delete(pdfUrl);
                kdrModel.removeAllValuesOfProperty(ZdoTerms.pdfUrl);
            }
            if(epubUrl != null) {
                store.delete(epubUrl);
                kdrModel.removeAllValuesOfProperty(ZdoTerms.epubUrl);
            }
            store.update(kdrModel);
        }
        else {
            Integer currentChildCount = triplestoreStuff.countDocChildren(store.removeTransactionFromUrl(model.getUrl()));
            if(callerRemovedChild) {
                currentChildCount -= 1;
            }
            if(currentChildCount <= 0) {
                model.replaceValueOfProperty(ZdoTerms.group, ZdoGroup.UNPUBLISHED.name());
                model.removeAllValuesOfProperty(ZdoTerms.owner);
                store.update(model);
                didRemoveChild = true;

                //Unlock kdr object
                ZdoModel kdrModel = store.get(model.get(ZdoTerms.kdrObject));
                kdrModel.decreaseLockCount();
                store.update(kdrModel);
            }
        }

        //If we are root, unindex us
        if(ZdoType.isRootCategory(model.get(ZdoTerms.zdoType))) {
            rootsToReindex.add(store.removeTransactionFromUrl(model.getUrl()));
        }
        else {
            //Recurse on parent
            String parentUrl = model.getParent();
            if(parentUrl != null) {
                unpublishDocumentInner(store.get(parentUrl), didRemoveChild, rootsToReindex);
            }
        }
    }
}
