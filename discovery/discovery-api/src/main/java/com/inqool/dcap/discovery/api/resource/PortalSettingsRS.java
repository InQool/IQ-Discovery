package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.common.entity.PortalSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.PortalSettingsAccess;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/portal/")
@RequestScoped
public class PortalSettingsRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private PortalSettingsAccess portalSettingsAccess;

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
}
