package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.integration.service.DataStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Lukas Jane (inQool)
 */
@Path("/data/file")
public class GenericFileResource {
    @Inject
    private DataStore dataStore;

    @Path("/{uuid}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("uuid") String uuid) throws IOException {
        try {
            String url = dataStore.createUrl(uuid);
            URLConnection urlConnection = new URL(url).openConnection();
            String contentType = urlConnection.getContentType();
            InputStream in = urlConnection.getInputStream();
            return Response.ok(in, contentType).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
