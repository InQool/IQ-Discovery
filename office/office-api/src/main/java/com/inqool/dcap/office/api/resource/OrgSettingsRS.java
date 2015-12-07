package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.entity.OrganizationSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.core.OrganizationSettingsAccess;
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

/**
 * @author Lukas Jane (inQool) 1. 7. 2015.
 */
@Path("/orgSettings")
@LoggedIn
@RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
@RequestScoped
public class OrgSettingsRS {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OrganizationSettingsAccess organizationSettingsAccess;

    @Inject
    private DataStore store;

    @Path("/current/")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentOrgSettings() {
        try {
            OrganizationSettings orgSettings = organizationSettingsAccess.fetchCurrentOrgSettings();
            return Response.ok(orgSettings).build();
        } catch (Exception e) {
            logger.error("Failed while getting current organization settings.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{orgIdmId}/")
    @GET
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrgSettings(@PathParam("orgIdmId") String orgIdmId) {
        try {
            OrganizationSettings orgSettings = organizationSettingsAccess.fetchOrgSettings(orgIdmId);
            return Response.ok(orgSettings).build();
        } catch (Exception e) {
            logger.error("Could not get org settings.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/current/")
    @PUT
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCurrentOrgSettings(OrganizationSettings organizationSettings) {
        try {
            organizationSettingsAccess.updateCurrentOrgSettings(organizationSettings);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not update org settings.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/")
    @PUT
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrgSettings(OrganizationSettings organizationSettings, @PathParam("orgIdmId") String orgIdmId) {
        try {
            organizationSettingsAccess.updateOrgSettings(orgIdmId, organizationSettings);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not update org settings.", e);
            return Response.serverError().build();
        }
    }

    @Path("/current/header")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCurrentOrgHeader(MultipartFormDataInput input) {
        try {
            String uuid = organizationSettingsAccess.uploadHeader(input);
            return Response.ok(uuid).build();
        } catch (Exception e) {
            logger.error("Could not update org header.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/header")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadOrgHeader(MultipartFormDataInput input, @PathParam("orgIdmId") String orgIdmId) {
        try {
            String uuid = organizationSettingsAccess.uploadHeaderOfOrg(input, orgIdmId);
            return Response.ok(uuid).build();
        } catch (Exception e) {
            logger.error("Could not update org header.", e);
            return Response.serverError().build();
        }
    }

    @Path("/current/header")
    @DELETE
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    public Response deleteCurrentOrgHeader() {
        try {
            store.startTransaction();
            organizationSettingsAccess.deleteHeader();
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete org header.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/header")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deleteOrgHeader(@PathParam("orgIdmId") String orgIdmId) {
        try {
            store.startTransaction();
            organizationSettingsAccess.deleteHeaderOfOrg(orgIdmId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete org header.", e);
            return Response.serverError().build();
        }
    }

    @Path("/current/logo")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCurrentOrgLogo(MultipartFormDataInput input) {
        try {
            String uuid = organizationSettingsAccess.uploadLogo(input);
            return Response.ok(uuid).build();
        } catch (Exception e) {
            logger.error("Could not update org logo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/logo")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadOrgLogo(MultipartFormDataInput input, @PathParam("orgIdmId") String orgIdmId) {
        try {
            String uuid = organizationSettingsAccess.uploadLogoOfOrg(input, orgIdmId);
            return Response.ok(uuid).build();
        } catch (Exception e) {
            logger.error("Could not update org logo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/current/logo")
    @DELETE
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    public Response deleteCurrentOrgLogo() {
        try {
            store.startTransaction();
            organizationSettingsAccess.deleteLogo();
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete org logo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/logo")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deleteOrgLogo(@PathParam("orgIdmId") String orgIdmId) {
        try {
            store.startTransaction();
            organizationSettingsAccess.deleteLogoOfOrg(orgIdmId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete org logo.", e);
            return Response.serverError().build();
        }
    }

    @Path("/current/watermark")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCurrentOrgWatermark(MultipartFormDataInput input) {
        try {
            String uuid = organizationSettingsAccess.uploadWatermark(input);
            return Response.ok(uuid).build();
        } catch (Exception e) {
            logger.error("Could not update org watermark.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/watermark")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadOrgWatermark(MultipartFormDataInput input, @PathParam("orgIdmId") String orgIdmId) {
        try {
            String uuid = organizationSettingsAccess.uploadWatermarkOfOrg(input, orgIdmId);
            return Response.ok(uuid).build();
        } catch (Exception e) {
            logger.error("Could not update org watermark.", e);
            return Response.serverError().build();
        }
    }

    @Path("/current/watermark")
    @DELETE
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    public Response deleteCurrentOrgWatermark() {
        try {
            store.startTransaction();
            organizationSettingsAccess.deleteWatermark();
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete org watermark.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgIdmId}/watermark")
    @DELETE
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response deleteOrgWatermark(@PathParam("orgIdmId") String orgIdmId) {
        try {
            store.startTransaction();
            organizationSettingsAccess.deleteWatermarkOfOrg(orgIdmId);
            store.commitTransaction();
            return Response.ok().build();
        } catch (Exception e) {
            store.rollbackTransaction();
            logger.error("Could not delete org watermark.", e);
            return Response.serverError().build();
        }
    }
}
