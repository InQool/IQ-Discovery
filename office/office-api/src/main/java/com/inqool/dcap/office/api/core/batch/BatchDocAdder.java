package com.inqool.dcap.office.api.core.batch;

import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.YearNormalizer;
import com.inqool.dcap.common.StatsAccessCommon;
import com.inqool.dcap.common.dto.AdditionalMetadata;
import com.inqool.dcap.config.CustomProjectStageHolder;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.*;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.TriplestoreStuff;
import com.inqool.dcap.office.api.core.NkListsAccess;
import com.inqool.dcap.office.api.core.SckChoAttributeTranslator;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.office.api.util.ErrorConstants;
import com.inqool.dcap.office.api.util.OfficeException;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoUser;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.picketlink.Identity;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Lukas Jane (inQool) 25. 3. 2015.
 */
@RequestScoped
public class BatchDocAdder {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    private Identity identity;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Inject
    private NkListsAccess nkListsAccess;

    @Inject
    private StatsAccessCommon statsAccessCommon;

    @Inject
    private PicketLinkAccess plAccess;

    @Inject
    private ProjectStage projectStage;

    private Map<String, ZdoModel> docsAddedToBatchNow;

    private String userUnderWhichToAdd;

    public void setUser(String zdoUser) {
        userUnderWhichToAdd = zdoUser;
    }

    @Transactional
    public void addDocuments(List<String> documentIds, int batchId) throws IOException, OfficeException {
        if(documentIds.isEmpty()) {
            return;
        }
        if(userUnderWhichToAdd == null) {
            userUnderWhichToAdd = ((ZdoUser) identity.getAccount()).getLoginName();
        }
        ZdoBatch batch = em.find(ZdoBatch.class, batchId);
        docsAddedToBatchNow = new HashMap<>();
        if(batch == null || !batch.getOwner().equals(userUnderWhichToAdd)) {
            throw new RuntimeException("Bad batch id.");
        }
        if(ZdoBatch.BatchState.unfinished != batch.getState()) {
            throw new RuntimeException("Can't add documents to published or discarded batch.");
        }
        for(String docId : documentIds) {
            ZdoModel model = store.get(store.createUrl(docId));
            if(model == null) {
                throw new RuntimeException("Failed to retrieve such document from Fedora.");
            }
            if(!ZdoGroup.KDR.name().equals(model.get(ZdoTerms.group))) {
                throw new RuntimeException("Supplied document is not in state ORIGINAL.");
            }

            ZdoModel clone = findOrCreateConcept(model, batch);

            batch.getDocuments().add(store.getOnlyIdFromUrl(clone.getUrl()));
            batch.setModified(LocalDateTime.now());
        }

        //Record statistics
        String user = userUnderWhichToAdd;
        String org = plAccess.getOrganizationOfUser(plAccess.getUser(user)).getName();
        statsAccessCommon.documentConcepted(user, org, batch.getNumDocs());
    }

    private ZdoModel findOrCreateConcept(ZdoModel kdrModel, ZdoBatch batch) throws IOException, OfficeException {
        ZdoModel modelToClone = kdrModel;
        ZdoModel result;

        //Exception if someone else owns this
        checkOwnership(kdrModel);

        //Is document already concepted?
        String conceptedUrl = triplestoreStuff.getUrlOfConcepted(kdrModel);
        if(conceptedUrl != null) {
            ZdoModel conceptedModel = store.get(conceptedUrl);
            String conceptedBatchId = conceptedModel.get(ZdoTerms.batchId);
            if(ZdoType.isBranchEndCategory(kdrModel.get(ZdoTerms.zdoType))) {
                OfficeException ex = new OfficeException("This document is already concepted in some batch.");
                ex.setErrorCode(ErrorConstants.DOC_ALREADY_CONCEPTED);
                ZdoBatch conceptedBatch = em.find(ZdoBatch.class, Integer.valueOf(conceptedBatchId));
                ex.addParameter(conceptedBatch.getName());
                throw ex;
            }
            else {  //We are parent
                if(batch.getId() != Integer.valueOf(conceptedBatchId)) {
                    //Error, this parent is already concepted in different batch
                    OfficeException ex = new OfficeException("Can't add document to batch because its parent is concepted in different batch.");
                    ex.setErrorCode(ErrorConstants.PARENT_ALREADY_CONCEPTED_IN_DIFFERENT_BATCH);
                    ZdoBatch conceptedBatch = em.find(ZdoBatch.class, Integer.valueOf(conceptedBatchId));
                    ex.addParameter(conceptedBatch.getName());
                    throw ex;
                }
                return conceptedModel;  //It's ok if we are already concepted in this batch
            }
        }
        else {
            //Find if we created concept of parent right now in this request (2 issues of same volume added at once)
            if(docsAddedToBatchNow.keySet().contains(kdrModel.get(ZdoTerms.inventoryId))) {
                return docsAddedToBatchNow.get(kdrModel.get(ZdoTerms.inventoryId));
            }

            //If there is published version of this doc, clone that one instead
            //Also check document ownership
            String newestPublished;
            newestPublished = kdrModel.get(ZdoTerms.newestPublished);
            if(newestPublished != null) {
                modelToClone = store.get(newestPublished);
            }

            //Clone
            changeOwnershipAndLock(kdrModel);
            cloningItself(modelToClone, batch); //warning: changes modelToClone
            result = modelToClone;
        }

        //Do we have a parent?
        String kdrParentUrl = kdrModel.get(DCTerms.isPartOf);
        if(kdrParentUrl != null) {
            //We have a parent, retrieve it from Fedora
            ZdoModel kdrParent = store.get(kdrParentUrl);
            ZdoModel newParent = findOrCreateConcept(kdrParent, batch);
            //Set our parent value accordingly
            result.replaceValueOfProperty(DCTerms.isPartOf, store.removeTransactionFromUrl(newParent.getUrl()));
        }

        //Set validity to publish based on if required fields are filled in
        if(!ZdoType.isRootCategory(result.get(ZdoTerms.zdoType)) && result.get(DCTerms.title) != null) {
            result.replaceValueOfProperty(ZdoTerms.validToPublish, "true");
        }
        if(ZdoType.isRootCategory(result.get(ZdoTerms.zdoType)) && result.get(ZdoTerms.documentType) != null) {
            result.replaceValueOfProperty(ZdoTerms.validToPublish, "true");
        }

        //Now store us to Fedora and be done with it
        store.update(result);
        docsAddedToBatchNow.put(result.get(ZdoTerms.inventoryId), result);
        return result;
    }

    private void cloningItself(ZdoModel model, ZdoBatch batch) {
        //For CHOs in SCK, translate additional metadata to Custom attributes
        if(ZdoType.cho.name().equals(model.get(ZdoTerms.zdoType)) &&
                (CustomProjectStageHolder.StagingSCK.equals(projectStage)
                || CustomProjectStageHolder.ProductionSCK.equals(projectStage)
                || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage)
                        || CustomProjectStageHolder.DevelopmentKudlajz.equals(projectStage))) {
            reshovelSckAdditionalMetadataToCustomAttributes(model);
        }

        model.clearFedoraInsertedTriplets();    //Delete fedora autogenerated triplets, they will be generated again
        model.removeAllValuesOfProperty(ZdoTerms.lockCount);
        model.removeAllValuesOfProperty(ZdoTerms.newestPublished);
        model.removeAllValuesOfProperty(ZdoTerms.additionalMetadata);
        model.replaceValueOfProperty(ZdoTerms.group, ZdoGroup.ZDO_CONCEPT.name());  //Change type to concept
        model.replaceValueOfProperty(ZdoTerms.batchId, String.valueOf(batch.getId()));
        model.replaceValueOfProperty(ZdoTerms.owner, userUnderWhichToAdd);

        //If kdr object is not set (we create copy of kdr), remember kdr object url
        if(model.get(ZdoTerms.kdrObject) == null) {
            model.add(ZdoTerms.kdrObject, store.removeTransactionFromUrl(model.getUrl()));

            //Store some default fields for publishing
            if(ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) {
                model.replaceValueOfProperty(ZdoTerms.allowContentPublicly, "true");
                model.replaceValueOfProperty(ZdoTerms.allowPdfExport, "true");
                model.replaceValueOfProperty(ZdoTerms.allowEpubExport, "true");
                model.replaceValueOfProperty(ZdoTerms.watermark, "true");
                //In SCK, override defaults based on hint from kdr
                if(ZdoType.cho.name().equals(model.get(ZdoTerms.zdoType))
                        && (CustomProjectStageHolder.StagingSCK.equals(projectStage)
                        || CustomProjectStageHolder.ProductionSCK.equals(projectStage))) {
                    String publishHint = model.get(ZdoTerms.publishHint);
                    if(!PublishHint.allowDownload.name().equals(publishHint)) {
                        model.replaceValueOfProperty(ZdoTerms.allowPdfExport, "false");
                    }
                    if(PublishHint.metadataOnly.name().equals(publishHint) || PublishHint.dontPublish.name().equals(publishHint)) {
                        model.replaceValueOfProperty(ZdoTerms.allowContentPublicly, "false");
                    }
                }
            }
        }

        //Normalize created date
        String normalizedCreated = YearNormalizer.preNormalize(model.get(DCTerms.created));
        model.removeAllValuesOfProperty(DCTerms.created);
        if(normalizedCreated != null) {
            model.add(DCTerms.created, normalizedCreated);
        }

        //Unless this is CHO in SCK, filter official NK list values
        if(!((CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage) || CustomProjectStageHolder.DevelopmentLukess.equals(projectStage) || CustomProjectStageHolder.DevelopmentKudlajz.equals(projectStage)) && ZdoType.cho.name().equals(model.get(ZdoTerms.zdoType)))) {
            filterOnlyOfficialListValues(model);
        }

        //Generate new model URL and change subject info to reflect this
        String newModelUrl = store.createUrl(UUID.randomUUID().toString());
        model.setUrl(newModelUrl);
        model.resubjectTo(newModelUrl);
    }

    private void filterOnlyOfficialListValues(ZdoModel model) {
        //Allow only data from official NK lists into some fields
        //Type = genre
        List<String> types = model.getAll(DCTerms.type);
        model.removeAllValuesOfProperty(DCTerms.type);
        for (String type : types) {
            if(nkListsAccess.existsGenre(type)) {
                model.add(DCTerms.type, type);
            }
        }
        //Subject = topic
        List<String> subjects = model.getAll(DCTerms.subject);
        model.removeAllValuesOfProperty(DCTerms.subject);
        for (String subject : subjects) {
            if(nkListsAccess.existsTopic(subject)) {
                model.add(DCTerms.subject, subject);
            }
        }
        //Spatial = geographical
        List<String> spatials = model.getAll(DCTerms.spatial);
        model.removeAllValuesOfProperty(DCTerms.spatial);
        for (String spatial : spatials) {
            if(nkListsAccess.existsGeo(spatial)) {
                model.add(DCTerms.spatial, spatial);
            }
        }
        //Temporal = chronological
        List<String> temporals = model.getAll(DCTerms.temporal);
        model.removeAllValuesOfProperty(DCTerms.temporal);
        for (String temporal : temporals) {
            if(nkListsAccess.existsChro(temporal)) {
                model.add(DCTerms.temporal, temporal);
            }
        }
    }

    private void changeOwnershipAndLock(ZdoModel model) throws IOException, OfficeException {
        String currentOwner = model.get(ZdoTerms.owner);
        if(currentOwner == null) {
            model.replaceValueOfProperty(ZdoTerms.owner, userUnderWhichToAdd);
        }
        model.increaseLockCount();
        store.update(model);
    }

    private void checkOwnership(ZdoModel model) throws IOException, OfficeException {
        String currentOwner = model.get(ZdoTerms.owner);
        if(!(currentOwner == null || currentOwner.equals(userUnderWhichToAdd))) {
            OfficeException ex = new OfficeException("This document is owned by someone else.");
            ex.setErrorCode(ErrorConstants.DOCUMENT_OWNED_BY_OTHER);
            ex.addParameter(currentOwner);
            throw ex;
        }
    }

    private void reshovelSckAdditionalMetadataToCustomAttributes(ZdoModel model) {
        String type = model.get(DCTerms.type);
        AdditionalMetadata additionalMetadata = AdditionalMetadata.ofModel(model);
        Map<String, List<String>> metadataMap = additionalMetadata.getMetadataMap();
        int fieldIndex = 1;
        for (Map.Entry<String, List<String>> entry : metadataMap.entrySet()) {
            String translation = SckChoAttributeTranslator.translateAttribute(entry.getKey(), type);
            if(translation == null) {
                continue;
            }
            model.addAll(new PropertyImpl("http://inqool.cz/zdo/1.0/customField_" + fieldIndex), entry.getValue());
            model.add(new PropertyImpl("http://inqool.cz/zdo/1.0/customField_" + fieldIndex + "_name"), translation);
            fieldIndex++;
        }
    }
}
