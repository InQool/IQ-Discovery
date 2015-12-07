package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.common.entity.OrganizationSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.OrganizationSettingsAccess;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 1. 7. 2015.
 */
@Path("/orgSettings")
@RequestScoped
public class OrgSettingsRS {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OrganizationSettingsAccess organizationSettingsAccess;

    @Path("/{orgIdmId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrgSettings(@PathParam("orgIdmId") String orgIdmId) {
        try {
            OrganizationSettings orgSettings = organizationSettingsAccess.fetchOrgSettings(orgIdmId);
            return Response.ok(orgSettings).build();
        } catch(Exception e) {
            logger.error("Could not get org settings.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
