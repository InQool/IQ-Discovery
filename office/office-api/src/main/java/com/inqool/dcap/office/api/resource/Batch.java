package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.oai.harvester.OaiHarvester;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.core.batch.BatchBasicOperations;
import com.inqool.dcap.office.api.core.batch.BatchDocAdder;
import com.inqool.dcap.office.api.core.batch.BatchDocDeleter;
import com.inqool.dcap.office.api.core.batch.BatchPublisher;
import com.inqool.dcap.office.api.dto.ZdoDocumentBrief;
import com.inqool.dcap.office.api.entity.ZdoBatch;
import com.inqool.dcap.office.api.util.OfficeException;
import com.inqool.dcap.office.indexer.indexer.SolrBulkIndexer;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.ZdoRoles;
import org.picketlink.Identity;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.picketlink.idm.PartitionManager;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 3. 3. 2015.
 */
@Path("/batch")
@LoggedIn
@RequestScoped
public class Batch {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Inject
    private BatchBasicOperations batchBasicOperations;
    @Inject
    private BatchDocAdder batchDocAdder;
    @Inject
    private BatchPublisher batchPublisher;
    @Inject
    private BatchDocDeleter batchDocDeleter;

    @Inject
    private Identity identity;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    @Inject
    private SolrBulkIndexer indexer;

    @Inject
    private OaiHarvester oaiHarvester;

    @Path("/")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    @Produces(MediaType.APPLICATION_JSON)
    public int createNewBatch(@QueryParam("name") String name) {
        return batchBasicOperations.createNewBatch(name);
    }

    @Path("/")
    @GET
    @RolesAllowed(ZdoRoles.CURATOR)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ZdoBatch> listBiatches(@QueryParam("state") String state) {
        if(state == null) state = "all";
        else {
            try {
                state = ZdoBatch.BatchState.valueOf(state).name();
            } catch(IllegalArgumentException e) {
                logger.error("Batch state from request not recognized, defaulting to all.");
                state = "all";
            }
        }
        return batchBasicOperations.listBatches(state);
    }

    @Path("/ofUser/{idmId}/")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_ORG, ZdoRoles.ADMIN_SYS})
    @Produces(MediaType.APPLICATION_JSON)
    public List<ZdoBatch> listBatches(@PathParam("idmId") String idmId, @QueryParam("state") String state) {
        return batchBasicOperations.listBatchesOfUser(idmId, state);
    }

    @Path("/{batchId}/")
    @GET
    @RolesAllowed(ZdoRoles.CURATOR)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBatch(@PathParam("batchId") int batchId) {
        ZdoBatch batch = batchBasicOperations.getBatch(batchId);
        if(batch == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(batch).build();
    }

    @Path("/{batchId}/document")
    @GET
    @RolesAllowed(ZdoRoles.CURATOR)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listBatchDocuments(@PathParam("batchId") int batchId) {
        List<ZdoDocumentBrief> docList = batchBasicOperations.listBatchDocuments(batchId);
        if(docList == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(docList).build();
    }

    @Path("/{batchId}/tree")
    @GET
    @RolesAllowed(ZdoRoles.CURATOR)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBatchDocumentTree(@PathParam("batchId") int batchId) {
        Map<String, Object> docList = batchBasicOperations.getBatchDocumentTree(batchId);
        if(docList == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(docList).build();
    }

    @Path("/{batchId}/publish")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    public Response publishBatch(@PathParam("batchId") int batchId) throws IOException {
        store.startTransaction();
        try {
            batchPublisher.publishBatch(batchId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not publish batch " + batchId + ".", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{batchId}/")
    @DELETE
    @RolesAllowed(ZdoRoles.CURATOR)
    public Response discardBatch(@PathParam("batchId") int batchId) throws IOException {
        store.startTransaction();
        try {
            batchDocDeleter.discardBatch(batchId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not discard batch.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/delete")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response discardBatches(List<Integer> batchIds) throws IOException {
        store.startTransaction();
        try {
            batchDocDeleter.discardBatches(batchIds);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not discard document.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{batchId}/document/delete")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response discardDocuments(List<String> docIds, @PathParam("batchId") int batchId) throws IOException {
        store.startTransaction();
        try {
            batchDocDeleter.deleteDocuments(docIds, batchId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not discard documents.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{batchId}/document/unpublish")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unpublishDocument(String docId, @PathParam("batchId") int batchId) throws IOException {
        store.startTransaction();
        try {
            List<String> rootsToReindex = batchDocDeleter.unpublishDocument(docId, batchId);
            store.commitTransaction();
            //Solr reindex must be after fedora commit
            for(String rootToReindex : rootsToReindex) {
                indexer.updateUri(rootToReindex);
            }
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not unpublish document.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{batchId}/document/move/{targetBatchId}")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveDocumentsToBatch(List<String> docUrls, @PathParam("batchId") int batchId, @PathParam("targetBatchId") int targetBatchId) throws IOException {
        store.startTransaction();
        try {
            batchBasicOperations.moveDocumentsToBatch(docUrls, batchId, targetBatchId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not move documents to another batch.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{batchId}/document")
    @POST
    @RolesAllowed(ZdoRoles.CURATOR)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDocuments(List<String> documentIds, @PathParam("batchId") int batchId) {
        store.startTransaction();
        try {
            batchDocAdder.addDocuments(documentIds, batchId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (OfficeException e) {
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).entity(e.serialize()).build();
        } catch (Exception e) {
            logger.error("Could not add documents to batch.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/handOver/")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_ORG, ZdoRoles.ADMIN_SYS, ZdoRoles.CURATOR})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handOverBatch(List<Integer> batchIds, @QueryParam("sourceUserId") String sourceUserId, @QueryParam("targetUserId") String targetUserId) throws IOException {
        store.startTransaction();
        try {
            batchBasicOperations.handOverBatch(batchIds, sourceUserId, targetUserId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (OfficeException e) {
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).entity(e.serialize()).build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not give batch to other user.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/handOverAllBatches")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_ORG, ZdoRoles.ADMIN_SYS})
    public Response handOverAllBatches(@QueryParam("sourceUserId") String sourceUserId, @QueryParam("targetUserId") String targetUserId) throws IOException {
        store.startTransaction();
        try {
            batchBasicOperations.handOverAllBatches(sourceUserId, targetUserId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not give batch to other user.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
