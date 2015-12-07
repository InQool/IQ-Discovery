package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.discovery.api.core.OrganizationSettingsAccess;
import com.inqool.dcap.integration.service.DataStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Lukas Jane (inQool)
 */
@Path("/data/watermark")
public class WatermarkResource {
    @Inject
    private DataStore dataStore;

    @Inject
    private OrganizationSettingsAccess organizationSettingsAccess;

    @Path("/{orgIdmId}")
    @GET
    @Produces("image/png")
    public Response getWatermark(@PathParam("orgIdmId") String orgIdmId) throws IOException {
        try {
            String url = dataStore.createUrl(organizationSettingsAccess.fetchOrgWatermarkId(orgIdmId));
            InputStream in = new URL(url).openStream();
            return Response.ok(in).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
