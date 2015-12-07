package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.OrganizationAccess;
import com.inqool.dcap.office.api.dto.OrganizationDto;
import com.inqool.dcap.office.api.dto.UserDto;
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
@Path("/organization")
@LoggedIn
@RequestScoped
public class Organization {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OrganizationAccess organizationAccess;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOrgs() {
        try {
            List<OrganizationDto> orgs = organizationAccess.listAllOrgs();
            return Response.ok(orgs).build();
        } catch(Exception e) {
            logger.error("Could not list organizations.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgId}/")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrg(@PathParam("orgId") String orgId) {
        try {
            OrganizationDto org = organizationAccess.getOrg(orgId);
            return Response.ok(org).build();
        } catch(Exception e) {
            logger.error("Could not list organizations.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgId}/user/")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOrgUsers(@PathParam("orgId") String orgId) {
        try {
            List<UserDto> users = organizationAccess.listOrgUsers(orgId);
            return Response.ok(users).build();
        } catch(Exception e) {
            logger.error("Could list organization users.", e);
            return Response.serverError().build();
        }
    }

    @Path("/resync/")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response resyncOrgs() {
        try {
            organizationAccess.resyncAllOrgs();
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not resync organizations.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{orgId}/resync/")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
    public Response resyncOrg(@PathParam("orgId") String orgId) {
        try {
            organizationAccess.resyncOrg(orgId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not resync organization.", e);
            return Response.serverError().build();
        }
    }
}
