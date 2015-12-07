package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.discovery.api.core.OrganizationSettingsAccess;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Lukas Jane (inQool)
 */
@Path("/data/css")
public class CssResource {

    @Inject
    private OrganizationSettingsAccess organizationSettingsAccess;

    @Path("/{orgIdmId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCss(@PathParam("orgIdmId") String orgIdmId) throws IOException {
        try {
            return Response.ok(organizationSettingsAccess.fetchOrgCss(orgIdmId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
