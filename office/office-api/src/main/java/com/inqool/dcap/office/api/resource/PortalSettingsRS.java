package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.entity.PortalSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.core.PortalSettingsAccess;
import com.inqool.dcap.security.ZdoRoles;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/portal/")
@RequestScoped
@RolesAllowed(ZdoRoles.ADMIN_SYS)
public class PortalSettingsRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private PortalSettingsAccess portalSettingsAccess;

    @Inject
    private DataStore store;

    @Path("/settings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPortalSettings() {
        try {
            PortalSettings portalSettings = portalSettingsAccess.fetchPortalSettings();
            return Response.ok(portalSettings).build();
        } catch (Exception e) {
            logger.error("Failed while getting portal settings.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/settings")
    @PUT
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePortalSettings(PortalSettings portalSettings) {
        try {
            portalSettingsAccess.updatePortalSettings(portalSettings);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while updating portal settings.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/header")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPortalHeader(MultipartFormDataInput input) {
        try {
            String uuid = portalSettingsAccess.uploadPortalHeader(input);
            return Response.ok(uuid).build();
        } catch(Exception e) {
            logger.error("Could not update portal header.", e);
            return Response.serverError().build();
        }
    }

    @Path("/header")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deletePortalHeader() {
        try {
            store.startTransaction();
            portalSettingsAccess.deletePortalHeader();
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete portal header.", e);
            return Response.serverError().build();
        }
    }

    @Path("/logo")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPortalLogo(MultipartFormDataInput input) {
        try {
            String uuid = portalSettingsAccess.uploadPortalLogo(input);
            return Response.ok(uuid).build();
        } catch(Exception e) {
            logger.error("Could not update portal logo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/logo")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deletePortalLogo() {
        try {
            store.startTransaction();
            portalSettingsAccess.deletePortalLogo();
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete portal logo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/watermark")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPortalWatermark(MultipartFormDataInput input) {
        try {
            String uuid = portalSettingsAccess.uploadPortalWatermark(input);
            return Response.ok(uuid).build();
        } catch(Exception e) {
            logger.error("Could not update portal watermark.", e);
            return Response.serverError().build();
        }
    }

    @Path("/watermark")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deletePortalWatermark() {
        try {
            store.startTransaction();
            portalSettingsAccess.deletePortalWatermark();
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete portal watermark.", e);
            return Response.serverError().build();
        }
    }
}
