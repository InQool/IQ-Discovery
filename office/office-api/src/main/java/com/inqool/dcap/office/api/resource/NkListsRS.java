package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.NkListsAccess;
import com.inqool.dcap.security.ZdoRoles;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */

@Path("/nkLists/")
@RolesAllowed(ZdoRoles.CURATOR)
@RequestScoped
public class NkListsRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private NkListsAccess nkListsAccess;

    @Path("/geo/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchGeoAuthority(@QueryParam("name") String name) {
        try {
            List<String> names = nkListsAccess.searchProxGeo(name);
            return Response.ok(names).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/geo/exists")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response existsGeoAuthority(@QueryParam("name") String name) {
        try {
            boolean exists = nkListsAccess.existsGeo(name);
            return Response.ok(exists).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/chro/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchChroAuthority(@QueryParam("name") String name) {
        try {
            List<String> names = nkListsAccess.searchProxChro(name);
            return Response.ok(names).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/chro/exists")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response existsChroAuthority(@QueryParam("name") String name) {
        try {
            boolean exists = nkListsAccess.existsChro(name);
            return Response.ok(exists).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topic/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchTopicAuthority(@QueryParam("name") String name) {
        try {
            List<String> names = nkListsAccess.searchProxTopic(name);
            return Response.ok(names).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topic/exists")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response existsTopicAuthority(@QueryParam("name") String name) {
        try {
            boolean exists = nkListsAccess.existsTopic(name);
            return Response.ok(exists).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/genre/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchGenreAuthority(@QueryParam("name") String name) {
        try {
            List<String> names = nkListsAccess.searchProxGenre(name);
            return Response.ok(names).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/geo/exists")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response existsGenreAuthority(@QueryParam("name") String name) {
        try {
            boolean exists = nkListsAccess.existsGenre(name);
            return Response.ok(exists).build();
        } catch (Exception e) {
            logger.error("Failed searching NK authority list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
