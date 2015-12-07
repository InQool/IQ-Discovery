package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.ExpoAccess;
import com.inqool.dcap.common.entity.Expo;
import com.inqool.dcap.security.ZdoRoles;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 1. 7. 2015.
 */
@Path("/expo")
@LoggedIn
@RolesAllowed(ZdoRoles.CURATOR)
@RequestScoped
public class ExpoRS {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private ExpoAccess expoAccess;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listExpos() {
        try {
            List<Expo> expos = expoAccess.listExpos();
            return Response.ok(expos).build();
        } catch (Exception e) {
            logger.error("Failed while getting expo list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewExpo(Expo expo) {
        try {
            expoAccess.createExpo(expo);
            return Response.ok(expo).build();
        } catch (Exception e) {
            logger.error("Failed while creating expo.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{expoId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpo(@PathParam("expoId") int expoId) {
        try {
            Expo expo = expoAccess.fetchExpo(expoId);
            return Response.ok(expo).build();
        } catch(Exception e) {
            logger.error("Could not get expo.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{expoId}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateExpo(Expo expo, @PathParam("expoId") int expoId) {
        try {
            expoAccess.updateExpo(expoId, expo);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update expo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/delete/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteExpos(List<Integer> expoIds) {
        try {
            expoAccess.deleteExpos(expoIds);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete expos.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{expoId}/")
    @DELETE
    public Response deleteExpo(@PathParam("expoId") int expoId) {
        try {
            expoAccess.deleteExpo(expoId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete expo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{expoId}/image/")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadExpoImage(MultipartFormDataInput input, @PathParam("expoId") int expoId) {
        try {
            expoAccess.uploadExpoImage(input, expoId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update expo image.", e);
            return Response.serverError().build();
        }
    }
}
