package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.common.entity.Expo;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.ExpoAccess;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/expo")
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
    public Response listExpoHeaders() {
        try {
            List<Expo> expos = expoAccess.listPublishedExpoHeaders();
            return Response.ok(expos).build();
        } catch (Exception e) {
            logger.error("Failed while getting expo list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/full")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listExpos() {
        try {
            List<Expo> expos = expoAccess.listPublishedExpos();
            return Response.ok(expos).build();
        } catch (Exception e) {
            logger.error("Failed while getting expo list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{expoId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpo(@PathParam("expoId") int expoId) {
        try {
            Expo expo = expoAccess.fetchPublishedExpo(expoId);
            return Response.ok(expo).build();
        } catch(Exception e) {
            logger.error("Could not get expo.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
