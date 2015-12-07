package com.inqool.dcap.office.api.core.batch;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.common.FeedAccess;
import com.inqool.dcap.common.StatsAccessCommon;
import com.inqool.dcap.common.entity.FeedEntry;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.exception.ExWrapper;
import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.TriplestoreStuff;
import com.inqool.dcap.office.api.core.StatsAccess;
import com.inqool.dcap.office.api.dto.FeedDataHolder;
import com.inqool.dcap.office.api.dto.ModelTreeNode;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.office.indexer.indexer.SolrBulkIndexer;
import com.inqool.dcap.security.PicketLinkAccess;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
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
public class BatchPublisher {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "ip.endpoint")
    private String IP_ENDPOINT;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    @Inject
    @ConfigProperty(name = "discovery.web.endpoint")
    private String DISCOVERY_WEB_ENDPOINT;

    @Inject
    private SolrBulkIndexer indexer;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Inject
    private FeedAccess feedAccess;

    @Inject
    private StatsAccess statsAccess;

    @Inject
    private StatsAccessCommon statsAccessCommon;

    @Inject
    private PicketLinkAccess plAccess;

    private List<String> rootsToDelete;
    private List<String> imagesToTile;

    //This maintains index to lookup modeltree by its url
    //we need it to hang more children on it
    private Map<String, ModelTreeNode> modelTreeNodeIndex;
    //This maintains index to lookup modeltree by its kdr doc url
    //we need it if we adopt document but then publish our own version, to find the adopted and set it to unpublished
    private Map<String, ModelTreeNode> modelTreeNodeKdrIndex;

    private List<ModelTreeNode> dataToIndex;

    private String userUnderWhichToPublish;

    public void setUser(String zdoUser) {
        userUnderWhichToPublish = zdoUser;
    }

    @Transactional
    public void publishBatch(int batchId) throws IOException {
        if(userUnderWhichToPublish == null) {
            userUnderWhichToPublish = plAccess.getUser().getLoginName();
        }

        ZdoBatch batch = em.find(ZdoBatch.class, batchId);

        if(batch == null || batch.isDeleted()/*todo || !batch.getOwner().equals(identity.getAccount().getId())*/) {
            throw new RuntimeException("Bad batch id to publish.");
        }

        if(!batch.getState().equals(ZdoBatch.BatchState.unfinished)) {
            throw new RuntimeException("Cant publish discarded or published batch.");
        }

        //Statistics collectors
        Map<String, Integer> zdoTypesCount = new HashMap<>();
        Map<String, Integer> documentTypesCount = new HashMap<>();
        Map<String, Integer> documentSubTypesCount = new HashMap<>();

        rootsToDelete = new ArrayList<>();
        imagesToTile = new ArrayList<>();
        modelTreeNodeIndex = new HashMap<>();
        modelTreeNodeKdrIndex = new HashMap<>();
        dataToIndex = new ArrayList<>();
        List<FeedDataHolder> feedData = new ArrayList<>();    //Rss and Atom feed data about newly published documents
        //Construct a forest from models that need to be published - bottom to top
        for(String docId : batch.getDocuments()) {
            ZdoModel model = store.get(store.createUrl(docId));

            storeStatistic(model, ZdoTerms.zdoType, zdoTypesCount);

            feedData.add(createFeedData(model));
            addNodeToStructure(model);
        }

        //Store stats - doctypes of root models
        for (ModelTreeNode modelTreeNode : dataToIndex) {
            ZdoModel model = modelTreeNode.getModel();
            if(ZdoType.periodical.name().equals(model.get(ZdoTerms.zdoType))) { //periodicals are not counted when end branch docs are, so we must count them now
                storeStatistic(model, ZdoTerms.zdoType, zdoTypesCount);
            }
            storeStatistic(model, ZdoTerms.documentType, documentTypesCount);
            storeStatistic(model, ZdoTerms.documentSubType, documentSubTypesCount);
        }

        //Unpublish documents being replaced and adopt their children - top to bottom
        for (ModelTreeNode modelTreeNode : dataToIndex) {
            resolveRoots(modelTreeNode);
        }

        //After all is done in fedora, index it in Solr
        rootsToDelete.forEach(ExWrapper.accept(indexer::remove));
        dataToIndex.forEach(ExWrapper.accept(indexer::update));

        //Aaand, tile the images
        Response response = ClientBuilder.newClient().target(IP_ENDPOINT + "process").request().post(Entity.json(imagesToTile));
        if(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException("Failed to call image processing war.");
        }

        batch.setState(ZdoBatch.BatchState.published);
        batch.setModified(LocalDateTime.now());

        //Record statistics
        String org = plAccess.getOrganizationOfUser(plAccess.getUser(userUnderWhichToPublish)).getName();
        statsAccess.incrementUserDocsPublished(userUnderWhichToPublish, batch.getNumDocs());
        statsAccess.incrementOrganizationDocsPublished(org, batch.getNumDocs());
        statsAccessCommon.documentPublished(userUnderWhichToPublish, org, batch.getNumDocs());
        for(String zdoTypeStr : zdoTypesCount.keySet()) {
            statsAccess.incrementZdoTypeUsage(ZdoType.valueOf(zdoTypeStr), org, zdoTypesCount.get(zdoTypeStr));
        }
        for(String docTypeStr : documentTypesCount.keySet()) {
            statsAccess.incrementDocTypeUsage(Integer.valueOf(docTypeStr), org, documentTypesCount.get(docTypeStr));
        }
        for(String docSubTypeStr : documentSubTypesCount.keySet()) {
            statsAccess.incrementDocSubTypeUsage(Integer.valueOf(docSubTypeStr), org, documentSubTypesCount.get(docSubTypeStr));
        }

        createFeedEntryAboutBatch(feedData);
    }

    //Takes prop value from model, increments its count in statCollector, or inserts count 1 if missing
    private void storeStatistic(ZdoModel model, Property prop, Map<String, Integer> statCollector) {
        String statVal = model.get(prop);
        if(statVal == null) {   //Maybe, like in SCK, document types are not filled, so do not record
            return;
        }
        Integer statValCount = statCollector.get(statVal);
        if(statValCount == null) {
            statCollector.put(statVal, 1);
        }
        else {
            statCollector.put(statVal, statValCount + 1);
        }
    }

    private ModelTreeNode addNodeToStructure(ZdoModel model) throws IOException {
        //Was this node already parsed?
        ModelTreeNode modelTreeNode = modelTreeNodeIndex.get(store.removeTransactionFromUrl(model.getUrl()));
        if(modelTreeNode != null) {
            return modelTreeNode;
        }
        //Can it be published?
        if(!"true".equals(model.get(ZdoTerms.validToPublish))) {
            throw new RuntimeException("This model is not ready for publishing!");
        }

        //Create tree structure with model for Solr
        modelTreeNode = new ModelTreeNode();
        modelTreeNode.setModel(model);
        modelTreeNodeIndex.put(store.removeTransactionFromUrl(model.getUrl()), modelTreeNode);
        modelTreeNodeKdrIndex.put(model.get(ZdoTerms.kdrObject), modelTreeNode);

        //Parse ancestors and add us as their children
        String parentUrl = model.get(DCTerms.isPartOf);
        if(parentUrl != null) {
            ZdoModel parent = store.get(parentUrl);
            ModelTreeNode parentNode = addNodeToStructure(parent);
            parentNode.getChildren().add(modelTreeNode);
        }
        else {
            //This is a root
            dataToIndex.add(modelTreeNode);
        }

        //Published periodicals and volumes shouldn't have batch
        if(!ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) {
            model.removeAllValuesOfProperty(ZdoTerms.batchId);
        }

        //Mark doc as published
        model.replaceValueOfProperty(ZdoTerms.group, ZdoGroup.ZDO.name());
        store.update(model);

        //Leaf children like page and binary don't need to be changed, but images must be tiled
        if(ZdoType.isBranchEndCategory(model.get(ZdoTerms.zdoType))) {
            imagesToTile.addAll(triplestoreStuff.fetchUrlsOfTileableImageDescendants(model.get(ZdoTerms.kdrObject)));
        }

        return modelTreeNode;
    }

    private void resolveRoots(ModelTreeNode modelTreeNode) throws IOException {
        ZdoModel model = modelTreeNode.getModel();

        //Get kdr version of this doc
        String kdrUrl = model.get(ZdoTerms.kdrObject);
        ZdoModel kdrDoc = store.get(kdrUrl);

        //If there is also a published version of this doc, find it, remove it and adopt all its children
        String oldModelUrl = kdrDoc.get(ZdoTerms.newestPublished);
        if(oldModelUrl != null) {
            //Mark the old published doc as unpublished
            ZdoModel oldModel = store.get(oldModelUrl);
            if (ZdoGroup.ZDO.name().equals(oldModel.get(ZdoTerms.group))) {
                markAsUnpublished(oldModel, kdrDoc);
                store.update(oldModel);
            }
        }
        resolveChildren(modelTreeNode, oldModelUrl);

        //Set to KDR doc that this will be its published version
        kdrDoc.replaceValueOfProperty(ZdoTerms.newestPublished, store.removeTransactionFromUrl(model.getUrl()));
        store.update(kdrDoc);
    }

    private void resolveChildren(ModelTreeNode modelTreeNode, String oldModelUrl) throws IOException {
        ZdoModel model = modelTreeNode.getModel();

        //If there is also a published version of this doc, find it, remove it and adopt all its children
        if(oldModelUrl != null) {
            //Change its children to be ours
            String queryString = "SELECT ?subject WHERE {\n" +
                    " ?subject <" + DCTerms.isPartOf.getURI() + "> <" + oldModelUrl + ">.\n" +
                    "}";
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
            ResultSet resultSet = queryExecution.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();
                RDFNode childNode = querySolution.get("subject");
                String childToAdoptUrl = childNode.asResource().getURI();
                ZdoModel childModel = store.get(childToAdoptUrl);
                childModel.replaceValueOfProperty(DCTerms.isPartOf, store.removeTransactionFromUrl(model.getUrl()));

                //If this children was published
                if(ZdoGroup.ZDO.name().equals(childModel.get(ZdoTerms.group))) {
                    //Is this children getting replaced by newer version?
                    if(modelTreeNodeKdrIndex.containsKey(childModel.get(ZdoTerms.kdrObject))) {
                        //Yes, unpublish it
                        ZdoModel childKdrObject = store.get(childModel.get(ZdoTerms.kdrObject));
                        markAsUnpublished(childModel, childKdrObject);
                        store.update(childKdrObject);
                    }
                    else {  //No, it should be added to our tree to solr
                        ModelTreeNode childModelTreeNode = new ModelTreeNode();
                        childModelTreeNode.setModel(childModel);
                        modelTreeNodeKdrIndex.put(childModel.get(ZdoTerms.kdrObject), childModelTreeNode);
                        modelTreeNodeIndex.put(store.removeTransactionFromUrl(childModel.getUrl()), modelTreeNode);
                        modelTreeNode.getChildren().add(childModelTreeNode);
                    }
                }
                store.update(childModel);
            }
        }

        //Recurse on children
        for(ModelTreeNode childNode : modelTreeNode.getChildren()) {
            //Get kdr version of this doc
            ZdoModel kdrDoc = store.get(childNode.getModel().get(ZdoTerms.kdrObject));
            resolveChildren(childNode, kdrDoc.get(ZdoTerms.newestPublished));
            kdrDoc.replaceValueOfProperty(ZdoTerms.newestPublished, store.removeTransactionFromUrl(childNode.getModel().getUrl()));
            store.update(kdrDoc);
        }
    }

    private void markAsUnpublished(ZdoModel model, ZdoModel kdrModel) throws IOException {
        if(ZdoGroup.ZDO.name().equals(model.get(ZdoTerms.group))) {
            model.replaceValueOfProperty(ZdoTerms.group, ZdoGroup.UNPUBLISHED.name());

            //Unlock kdr object
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

            //And unindex original published doc
            if (ZdoType.isRootCategory(model.get(ZdoTerms.zdoType))) {
                rootsToDelete.add(store.getOnlyIdFromUrl(model.getUrl()));
            }
        }
    }

    private FeedDataHolder createFeedData(ZdoModel model) {
        return new FeedDataHolder(model.get(DCTerms.title), model.get(ZdoTerms.inventoryId));
    }

    private void createFeedEntryAboutBatch(List<FeedDataHolder> feedData) {
        FeedEntry feedEntry = new FeedEntry();
        feedEntry.setTitle("Zveřejněny nové dokumenty");
        String description = "Byly zveřejněny nové dokumenty:<br/>\n";

        for (FeedDataHolder feedDataHolder : feedData) {
            String url = DISCOVERY_WEB_ENDPOINT + "/document/" + feedDataHolder.getInvId();
            if(feedEntry.getLink() == null) {
                feedEntry.setLink(url);
            }
            description += " <a href=\"" + url + "\">" + feedDataHolder.getTitle() + "</a><br/>\n";
        }
        feedEntry.setDescription(description);

        feedAccess.addToFeed(feedEntry);
    }
}
