package com.inqool.dcap.discovery.api.resource;

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
@Path("/data/epub")
public class EpubResource {
    @Inject
    private DataStore dataStore;

    @Path("/{uuid}")
    @GET
    @Produces("application/epub+zip")
    public Response getPdf(@PathParam("uuid") String uuid) throws IOException {
        try {
            String url = dataStore.createUrl(uuid);
            InputStream in = new URL(url).openStream();
            return Response.ok(in).header("Content-Disposition", "attachment; filename=" + uuid + ".epub").build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
