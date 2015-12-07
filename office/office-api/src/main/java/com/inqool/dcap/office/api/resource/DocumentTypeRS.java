package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.common.DocumentTypeAccess;
import com.inqool.dcap.common.entity.DocumentType;
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
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Path("/type")
@LoggedIn
@RequestScoped
public class DocumentTypeRS {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DocumentTypeAccess documentTypeAccess;

    @Path("/")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewType(@QueryParam("name") String name) {
        try {
            int id = documentTypeAccess.createType(name);
            return Response.ok(id).build();
        } catch(Exception e) {
            logger.error("Could not create new type.", e);
            return Response.serverError().build();
        }
    }

    @Path("/")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.CURATOR})
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAll() {
        try {
            List<Map<String, Object>> resultList = documentTypeAccess.listAll();
            return Response.ok(resultList).build();
        } catch(Exception e) {
            logger.error("Could not list types.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{typeId}/")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.CURATOR})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getType(@PathParam("typeId") int typeId) {
        try {
            DocumentType documentType = documentTypeAccess.fetchType(typeId);
            return Response.ok(documentType).build();
        } catch(Exception e) {
            logger.error("Could not update type.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{typeId}/")
    @PUT
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateType(@PathParam("typeId") int typeId, @QueryParam("name") String name) {
        try {
            documentTypeAccess.updateType(typeId, name);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update type.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{typeId}/")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deleteType(@PathParam("typeId") int typeId) {
        try {
            if(documentTypeAccess.deleteType(typeId)) {
                return Response.ok().build();
            }
            else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch(Exception e) {
            logger.error("Could not delete type.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{typeId}/subtype/")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewSubType(@PathParam("typeId") int typeId, @QueryParam("name") String name) {
        try {
            int id = documentTypeAccess.createSubType(typeId, name);
            return Response.ok(id).build();
        } catch(Exception e) {
            logger.error("Could not create new subtype.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{typeId}/subtype/{subTypeId}/")
    @PUT
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSubType(@PathParam("typeId") int typeId, @PathParam("subTypeId") int subTypeId, @QueryParam("name") String name) {
        try {
            documentTypeAccess.updateSubType(subTypeId, name);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update subType.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{typeId}/subtype/{subTypeId}/")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deleteSubType(@PathParam("typeId") int typeId, @PathParam("subTypeId") int subTypeId) {
        try {
            if(documentTypeAccess.deleteSubType(subTypeId)) {
                return Response.ok().build();
            }
            else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch(Exception e) {
            logger.error("Could not delete subType.", e);
            return Response.serverError().build();
        }
    }
}
