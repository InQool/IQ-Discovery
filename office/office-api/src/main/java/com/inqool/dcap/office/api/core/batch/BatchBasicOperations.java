package com.inqool.dcap.office.api.core.batch;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.SparqlTools;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.TriplestoreStuff;
import com.inqool.dcap.office.api.core.MailNotifier;
import com.inqool.dcap.office.api.dto.DocTreeNode;
import com.inqool.dcap.office.api.dto.ZdoDocumentBrief;
import com.inqool.dcap.office.api.entity.QZdoBatch;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.office.api.resource.Document;
import com.inqool.dcap.office.api.util.ErrorConstants;
import com.inqool.dcap.office.api.util.OfficeException;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.ZdoRoles;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.picketlink.Identity;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 25. 3. 2015.
 */
@RequestScoped
public class BatchBasicOperations {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    private DataStore store;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Inject
    private Identity identity;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    @Inject
    private MailNotifier mailNotifier;

    @Inject
    private SparqlTools sparqlTools;

    private final QZdoBatch qZdoBatch = QZdoBatch.zdoBatch;

    private String userUnderWhichToCreateBatch;

    public void setUser(String zdoUser) {
        userUnderWhichToCreateBatch = zdoUser;
    }

    @Transactional
    public int createNewBatch(String name) {
        if(userUnderWhichToCreateBatch == null) {
            userUnderWhichToCreateBatch = ((ZdoUser) identity.getAccount()).getLoginName();
        }

        ZdoBatch zdoBatch = new ZdoBatch();
        zdoBatch.setName(name);
        zdoBatch.setState(ZdoBatch.BatchState.unfinished);
        zdoBatch.setOwner(userUnderWhichToCreateBatch);
        em.persist(zdoBatch);
        em.flush();
        return zdoBatch.getId();
    }

    @Transactional
    public List<ZdoBatch> listBatches(String state) {
        return listBatchesOfUser(((ZdoUser) identity.getAccount()).getLoginName(), state);
    }

    @Transactional
    public List<ZdoBatch> listBatchesOfUser(String idmId, String state) {
        JPAQuery query = new JPAQuery(em);
        BooleanExpression conditions = qZdoBatch.deleted.eq(false)
                .and(qZdoBatch.owner.eq(idmId));
        //Filter batches by state
        if(state != null) {
            if("active".equals(state)) {
                conditions = conditions.and(qZdoBatch.state.eq(ZdoBatch.BatchState.published).or(qZdoBatch.state.eq(ZdoBatch.BatchState.unfinished)));
            }
            else {
                try {
                    state = ZdoBatch.BatchState.valueOf(state).name();
                } catch(IllegalArgumentException e) {
                    throw new RuntimeException("Unrecognized batch state: " + state);
                }
                conditions = conditions.and(qZdoBatch.state.eq(ZdoBatch.BatchState.valueOf(state)));
            }
        }

        List<ZdoBatch> batches = query
                .from(qZdoBatch)
                .where(conditions)
                .list(qZdoBatch);
        return batches;
    }

    @Transactional
    public ZdoBatch getBatch(int batchId) {
        ZdoBatch batch = em.find(ZdoBatch.class, batchId);
        if(batch == null || batch.isDeleted() || !batch.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            return null;
        }
        return batch;
    }

    @Transactional
    public List<ZdoDocumentBrief> listBatchDocuments(int batchId) {
        List<ZdoDocumentBrief> result = new ArrayList<>();

        ZdoBatch batch = em.find(ZdoBatch.class, batchId);
        if(batch == null || batch.isDeleted() || !batch.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            return null;
        }
        if(batch.getDocuments().isEmpty()) {
            return new ArrayList<>();
        }
        String queryString = "SELECT ?subject ?property ?value WHERE {\n" +
                "  ?subject ?property ?value." +
                "  ?subject <" + ZdoTerms.batchId.getURI() + "> " + ZdoTerms.stringConstantOf(String.valueOf(batchId)) + ".\n" +
                "}\n";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);

        Map<String, Map<String, List<String>>> subjectMap = sparqlTools.queryExecutionToPropertyMap(queryExecution);

        //For every document in batch
        for (Map.Entry<String, Map<String, List<String>>> stringMapEntry : subjectMap.entrySet()) {
            ZdoDocumentBrief zdoDocumentBrief = new ZdoDocumentBrief();
            Map<String, List<String>> propertyMap = stringMapEntry.getValue();

            //Fill in doc info
            zdoDocumentBrief.setId(store.getOnlyIdFromUrl(stringMapEntry.getKey()));
            zdoDocumentBrief.setInvId(propertyMap.get(ZdoTerms.inventoryId.getURI()).get(0));
            List<String> titles = propertyMap.get(DCTerms.title.getURI());
            if(titles == null || titles.isEmpty()) {
                zdoDocumentBrief.setTitle("Warning: Title missing.");
            }
            else {
                zdoDocumentBrief.setTitle(propertyMap.get(DCTerms.title.getURI()).get(0));
            }
            zdoDocumentBrief.setCreated(OffsetDateTime.parse(propertyMap.get(ZdoTerms.fedoraCreated.getURI()).get(0)).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
            zdoDocumentBrief.setModified(OffsetDateTime.parse(propertyMap.get(ZdoTerms.fedoraLastModified.getURI()).get(0)).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
            zdoDocumentBrief.setState(Document.zdoGroupToDocumentState(propertyMap.get(ZdoTerms.group.getURI()).get(0)));
            zdoDocumentBrief.setType(ZdoType.valueOf(propertyMap.get(ZdoTerms.zdoType.getURI()).get(0)));

            //Fill in batch info
            zdoDocumentBrief.setBatchId(batch.getId());
            zdoDocumentBrief.setBatchName(batch.getName());

            //Fill in validity for publishing
            List<String> validToPublishList = propertyMap.get(ZdoTerms.validToPublish.getURI());
            if(validToPublishList == null) {
                zdoDocumentBrief.setValidToPublish(false);
            }
            else {
                List<String> parentList = propertyMap.get(DCTerms.isPartOf.getURI());
                if(parentList != null) {
                    zdoDocumentBrief.setValidToPublish(triplestoreStuff.checkParentValidity(parentList.get(0)));
                }
                else {
                    zdoDocumentBrief.setValidToPublish(true);
                }
            }

            result.add(zdoDocumentBrief);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> getBatchDocumentTree(int batchId) {
        //Fetch document inventory ids from batch
        ZdoBatch batch = em.find(ZdoBatch.class, batchId);
        if(batch == null || batch.isDeleted() || !batch.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            return null;
        }
        List<String> docIds = batch.getDocuments();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("title", batch.getName());
        resultMap.put("folder", true);
        resultMap.put("expanded", true);

        //Reconstruct tree from them
        List<DocTreeNode> root = new ArrayList<>();
        resultMap.put("children", root);
        if(docIds.isEmpty()) {
            return resultMap;
        }

        Map<String, DocTreeNode> nodeIndex = new HashMap<>();   //Holds already parsed nodes so we can find if we have already parsed particular node and get its parsed representation
        for(String docId : docIds) {
            insertDocToNodeStructure(store.get(store.createUrl(docId)), root, nodeIndex);
        }
        return resultMap;
    }

    //Doc is inserted as child of existing node found in index, or as a root child
    private DocTreeNode insertDocToNodeStructure(ZdoModel model, List<DocTreeNode> root, Map<String, DocTreeNode> nodeIndex) {
        String title = model.get(DCTerms.title);
        String docId = store.getOnlyIdFromUrl(model.getUrl());
        DocTreeNode createdNode = new DocTreeNode(title, docId);

        //Fill in validToPublish state
        if("true".equals(model.get(ZdoTerms.validToPublish))) {
            createdNode.setValidToPublish(true);
        }
        else {
            createdNode.setValidToPublish(false);
        }

        String parentUrl = model.get(DCTerms.isPartOf);
        if(parentUrl != null) { //we have parent
            ZdoModel parentModel = store.get(parentUrl);
            String parentDocId = store.getOnlyIdFromUrl(parentModel.getUrl());
            if(nodeIndex.containsKey(parentDocId)) {  //parent was already added to the return structure
                nodeIndex.get(parentDocId).getChildren().add(createdNode);
            }
            else {  //parent must be recursively parsed to be added to the structure, only then can we put ourselves as its child
                DocTreeNode parent = insertDocToNodeStructure(parentModel, root, nodeIndex);
                parent.getChildren().add(createdNode);
            }
        }
        else {  //we are root type doc
            root.add(createdNode);
        }
        nodeIndex.put(docId, createdNode); //remember this node is already added (to put potential children on him)
        return createdNode;
    }

    @Transactional
    public void moveDocumentsToBatch(List<String> docIds, int batchId, int targetBatchId) throws IOException {
        ZdoBatch batch = em.find(ZdoBatch.class, batchId);
        ZdoBatch targetBatch = em.find(ZdoBatch.class, targetBatchId);

        String idmId = ((ZdoUser) identity.getAccount()).getLoginName();
        if (batch == null || targetBatch == null || !batch.getOwner().equals(idmId) || !targetBatch.getOwner().equals(idmId)) {
            throw new RuntimeException("Bad batch id.");
        }

        if (!batch.getState().equals(ZdoBatch.BatchState.unfinished) || !targetBatch.getState().equals(ZdoBatch.BatchState.unfinished)) {
            throw new RuntimeException("cant move between batches that are not in open state");
            //todo cant move between batches that are not in open state
        }
        List<String> batchDocumentUrls = batch.getDocuments();

        for (String docId : docIds) {
            ZdoModel model = store.get(store.createUrl(docId));
            String type = model.get(ZdoTerms.zdoType);
            if("issue".equals(type) || "periodical".equals(type) || "volume".equals(type)) {
                throw new RuntimeException("Periodical documents cant be moved to other batch.");
            }
            String invId = model.get(ZdoTerms.inventoryId);
            if (batchDocumentUrls.contains(docId)) {
                batchDocumentUrls.remove(docId);
                batch.setModified(LocalDateTime.now());
                targetBatch.getDocuments().add(docId);
                targetBatch.setModified(LocalDateTime.now());
                model.replaceValueOfProperty(ZdoTerms.batchId, String.valueOf(targetBatch.getId()));
                store.update(model);
            } else {
                throw new RuntimeException("Tried to move document (" + invId + ") that isn't in the batch " + (batch.getName()) + ".");
            }
        }
    }

    @Transactional
    public void handOverBatch(List<Integer> batchIds, String sourceUserId, String targetUserId) throws IOException, OfficeException {
        Set<ZdoRoles> thisUserRoles = picketLinkAccess.getUsersRoles();
        //Curator can only hand over his own documents
        if(!(thisUserRoles.contains(ZdoRoles.sys_admin) || thisUserRoles.contains(ZdoRoles.org_admin)) && thisUserRoles.contains(ZdoRoles.curator)) {
            sourceUserId = ((ZdoUser)identity.getAccount()).getLoginName();
        }

        //Check if target user is curator of the same organization
        ZdoUser targetUser = picketLinkAccess.getUser(targetUserId);
        ZdoOrganization sourceUserOrg = picketLinkAccess.getOrganizationOfUser(picketLinkAccess.getUser(sourceUserId));
        ZdoOrganization targetUserOrg = picketLinkAccess.getOrganizationOfUser(targetUser);
        if(!sourceUserOrg.getName().equals(targetUserOrg.getName())) {
            throw new RuntimeException("Target user is not of this organization.");
        }
        if(!picketLinkAccess.getRolesOfUser(targetUser).contains(ZdoRoles.curator)) {
            throw new RuntimeException("Target user is not a curator.");
        }

        //Fetch the batches
        JPAQuery query = new JPAQuery(em);
        BooleanExpression conditions = qZdoBatch.id.in(batchIds)
                .and(qZdoBatch.deleted.eq(false))
                .and(qZdoBatch.owner.eq(sourceUserId));
        List<ZdoBatch> batches = query
                .from(qZdoBatch)
                .where(conditions)
                .list(qZdoBatch);
        String batchesList = "";
        for(ZdoBatch batch : batches) {
            batch.setOwner(targetUserId);
            batchesList += batch.getName() + "<br />";
            List<String> docIds = batch.getDocuments();
            for(String docId : docIds) {    //TODO manage to move even if periodical is in multiple batches, but all those batches are getting moved
                changeDocOwner(store.createUrl(docId), targetUserId, false);
            }
        }

        mailNotifier.notifyBatchHandedOver(targetUser.getFirstName(), targetUser.getLastName(), batchesList, targetUser.getEmail());
    }

    @Transactional
    public void handOverAllBatches(String sourceUserId, String targetUserId) throws IOException, OfficeException {
        ZdoUser targetUser = picketLinkAccess.getUser(targetUserId);
        ZdoOrganization sourceUserOrg = picketLinkAccess.getOrganizationOfUser(picketLinkAccess.getUser(sourceUserId));
        ZdoOrganization targetUserOrg = picketLinkAccess.getOrganizationOfUser(targetUser);
        if(!sourceUserOrg.getName().equals(targetUserOrg.getName())) {
            throw new RuntimeException("Users do not belong to the same organization.");
        }

        if(!picketLinkAccess.getRolesOfUser(targetUser).contains(ZdoRoles.curator)) {
            throw new RuntimeException("Target user is not a curator.");
        }

        JPAQuery query = new JPAQuery(em);
        BooleanExpression conditions = qZdoBatch.deleted.eq(false)
                .and(qZdoBatch.owner.eq(sourceUserId));
        List<ZdoBatch> batches = query
                .from(qZdoBatch)
                .where(conditions)
                .list(qZdoBatch);
        String batchesList = "";
        for(ZdoBatch batch : batches) {
            batchesList += batch.getName() + "<br />";
            batch.setOwner(targetUserId);
            List<String> docIds = batch.getDocuments();
            for(String docId : docIds) {
                changeDocOwner(store.createUrl(docId), targetUserId, true);
            }
        }

        mailNotifier.notifyBatchHandedOver(targetUser.getFirstName(), targetUser.getLastName(), batchesList, targetUser.getEmail());
    }

    private void changeDocOwner(String docUrl, String targetUserId, boolean handingOverEverything) throws IOException, OfficeException {
        ZdoModel model = store.get(docUrl);
        String owner = model.get(ZdoTerms.owner);
        if(owner == null || owner.equals(targetUserId)) {
            return;
        }
        model.replaceValueOfProperty(ZdoTerms.owner, targetUserId);
        store.update(model);

        String kdrObjectUrl = model.get(ZdoTerms.kdrObject);
        if(kdrObjectUrl == null) {  //We are kdr object
            if(!handingOverEverything && model.getLockCount() > 1) {
                OfficeException officeException = new OfficeException("Can't hand over document, it is in multiple batches: " + model.get(ZdoTerms.inventoryId));
                officeException.setErrorCode(ErrorConstants.DOCUMENT_IN_MULTIPLE_BATCHES);
                throw officeException;
            }
        }
        else {  //We are non-kdr object
            //Run for kdr object
            changeDocOwner(kdrObjectUrl, targetUserId, handingOverEverything);
            //Run for parent
            String parentUrl = model.getParent();
            if(parentUrl != null) {
                changeDocOwner(parentUrl, targetUserId, handingOverEverything);
            }
        }
    }
}
