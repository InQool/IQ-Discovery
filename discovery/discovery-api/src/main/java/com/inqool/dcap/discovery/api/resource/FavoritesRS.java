package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.FavoritesAccess;
import com.inqool.dcap.discovery.api.dto.DocumentReservationDto;
import com.inqool.dcap.discovery.api.dto.DocumentReserveRequestDto;
import com.inqool.dcap.discovery.api.dto.FavoriteDocumentDto;
import com.inqool.dcap.discovery.api.entity.FavoriteQuery;
import com.inqool.dcap.discovery.api.exception.ReservedRecentlyException;
import org.picketlink.authorization.annotations.LoggedIn;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@LoggedIn
@Path("/")
@RequestScoped
public class FavoritesRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private FavoritesAccess favoritesAccess;

    //Favorite documents
    @Path("/auth/favDoc/")
    @POST
    public Response addFavDoc(@QueryParam("invId") String favDocInvId) {
        try {
            favoritesAccess.addFavDoc(favDocInvId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while adding favDoc.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/favDoc/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFavDocs() {
        try {
            Collection<FavoriteDocumentDto> favDocs = favoritesAccess.listFavDocs();
            return Response.ok(favDocs).build();
        } catch (Exception e) {
            logger.error("Failed while listing favDocs.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/favDoc/{id}")
    @DELETE
    public Response deleteFavDoc(@PathParam("id") int id) {
        try {
            favoritesAccess.deleteFavDoc(id);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while deleting favDoc.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    //Clipboard documents
    @Path("/auth/clipDoc/")
    @POST
    public Response addClipDoc(@QueryParam("invId") String clipDocInvId) {
        try {
            favoritesAccess.addClipDoc(clipDocInvId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while adding clipDoc.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/clipDoc/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listClipDocs() {
        try {
            Collection<FavoriteDocumentDto> clipDocs = favoritesAccess.listClipDocs();
            return Response.ok(clipDocs).build();
        } catch (Exception e) {
            logger.error("Failed while listing clipDocs.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/clipDoc/{id}")
    @DELETE
    public Response deleteClipDoc(@PathParam("id") int id) {
        try {
            favoritesAccess.deleteClipDoc(id);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while deleting clipDoc.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    //Favorite Solr queries
    @Path("/auth/favQuery/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addFavQuery(FavoriteQuery favoriteQuery) {
        try {
            favoritesAccess.addFavQuery(favoriteQuery);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while adding favQuery.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/favQuery/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFavQueries() {
        try {
            List<FavoriteQuery> favQueryList = favoritesAccess.listFavQueries();
            return Response.ok(favQueryList).build();
        } catch (Exception e) {
            logger.error("Failed while listing favQueries.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/favQuery/{id}")
    @DELETE
    public Response deleteFavQuery(@PathParam("id") int id) {
        try {
            favoritesAccess.deleteFavQuery(id);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while deleting favQuery.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    //Reservation
    @Path("/auth/reservation/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reserveDoc(DocumentReserveRequestDto docReserveRequestDto) {
        try {
            favoritesAccess.reserveDocs(docReserveRequestDto);
            return Response.ok().build();
        } catch (ReservedRecentlyException e) {
            logger.error("This document cannot be reserved again because it was reserved recently.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Failed while reserving documents.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/auth/reservation/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listReservedDocs() {
        try {
            Collection<DocumentReservationDto> documentReservationList = favoritesAccess.listReservedDocs();
            return Response.ok(documentReservationList).build();
        } catch (Exception e) {
            logger.error("Failed while listing reserved documents.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    //Query about a document
    @Path("/auth/query/")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response queryOnDoc(String query, @QueryParam("docId") String docId) {
        try {
            favoritesAccess.queryDoc(docId, query);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while doing a query about a document.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
