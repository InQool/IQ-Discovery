package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.ExternalSourcesAccess;
import com.inqool.dcap.common.entity.OaiSource;
import com.inqool.dcap.common.entity.SruSource;
import com.inqool.dcap.common.entity.Z3950Source;
import com.inqool.dcap.security.ZdoRoles;
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
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Path("/source")
@LoggedIn
@RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.REDACTOR})
@RequestScoped
public class Sources {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private ExternalSourcesAccess sourcesAccess;

    @Path("/oai/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewOaiSource(OaiSource oaiSource) {
        try {
            int id = sourcesAccess.createNewOaiSource(oaiSource);
            if (id < 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity(id).build();
            }

            return Response.ok(id).build();
        } catch(Exception e) {
            logger.error("Could not create new source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sru/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewSruSource(SruSource sruSource) {
        try {
            int id = sourcesAccess.createNewSruSource(sruSource);
            if (id < 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity(id).build();
            }

            return Response.ok(id).build();
        } catch(Exception e) {
            logger.error("Could not create new source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/z3950/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewZ3950Source(Z3950Source z3950Source) {
        try {
            int id = sourcesAccess.createNewZ3950Source(z3950Source);
            if (id < 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity(id).build();
            }

            return Response.ok(id).build();
        } catch(Exception e) {
            logger.error("Could not create new source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/oai/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOaiSources() {
        try {
            List<OaiSource> resultList = sourcesAccess.listOaiSources();
            return Response.ok(resultList).build();
        } catch(Exception e) {
            logger.error("Could not list sources.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sru/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listSruSources() {
        try {
            List<SruSource> resultList = sourcesAccess.listSruSources();
            return Response.ok(resultList).build();
        } catch(Exception e) {
            logger.error("Could not list sources.", e);
            return Response.serverError().build();
        }
    }

    @Path("/z3950/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listZ3950Sources() {
        try {
            List<Z3950Source> resultList = sourcesAccess.listZ3950Sources();
            return Response.ok(resultList).build();
        } catch(Exception e) {
            logger.error("Could not list sources.", e);
            return Response.serverError().build();
        }
    }

    @Path("/oai/{sourceId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOaiSource(@PathParam("sourceId") int sourceId) {
        try {
            OaiSource result = sourcesAccess.getOaiSource(sourceId);
            if(result == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(result).build();
        } catch(Exception e) {
            logger.error("Could not get source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sru/{sourceId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSruSource(@PathParam("sourceId") int sourceId) {
        try {
            SruSource result = sourcesAccess.getSruSource(sourceId);
            if(result == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(result).build();
        } catch(Exception e) {
            logger.error("Could not get source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/z3950/{sourceId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getZ3950Source(@PathParam("sourceId") int sourceId) {
        try {
            Z3950Source result = sourcesAccess.getZ3950Source(sourceId);
            if(result == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(result).build();
        } catch(Exception e) {
            logger.error("Could not get source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/oai/{sourceId}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOaiSource(OaiSource oaiSource, @PathParam("sourceId") int sourceId) {
        try {
            oaiSource.setId(sourceId);
            sourcesAccess.updateOaiSource(oaiSource);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sru/{sourceId}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOaiSource(SruSource sruSource, @PathParam("sourceId") int sourceId) {
        try {
            sruSource.setId(sourceId);
            sourcesAccess.updateSruSource(sruSource);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/z3950/{sourceId}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateZ3950Source(Z3950Source z3950Source, @PathParam("sourceId") int sourceId) {
        try {
            z3950Source.setId(sourceId);
            sourcesAccess.updateZ3950Source(z3950Source);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/oai/{sourceId}/")
         @DELETE
         public Response deleteOaiSource(@PathParam("sourceId") int sourceId) {
        try {
            sourcesAccess.deleteOaiSource(sourceId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sru/{sourceId}/")
    @DELETE
    public Response deleteSruSource(@PathParam("sourceId") int sourceId) {
        try {
            sourcesAccess.deleteSruSource(sourceId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete source.", e);
            return Response.serverError().build();
        }
    }

    @Path("/z3950/{sourceId}/")
    @DELETE
    public Response deleteZ3950Source(@PathParam("sourceId") int sourceId) {
        try {
            sourcesAccess.deleteZ3950Source(sourceId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete source.", e);
            return Response.serverError().build();
        }
    }
}
