package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Lukas Jane (inQool)
 */
@Path("/data/image/")
public class BackupProxy {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "imageserve.endpoint")
    private String IMAGESERVE_ENDPOINT;

    @Path("/{id}/image.dzi")
    @GET
    @Produces(MediaType.TEXT_XML)
    public Response getDescriptor(@PathParam("id") String id) throws FileNotFoundException {
        try {

            Response callResponse = ClientBuilder
                    .newClient()
                    .target(IMAGESERVE_ENDPOINT + "/data/image/" + id + "/image.dzi")
                    .request()
                    .get();
            if(callResponse.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                return Response.ok(callResponse.readEntity(String.class)).build();
            }
            else {
                logger.error("Image dzi proxy did not receive 200: " + callResponse.getStatus());
                return callResponse;
            }
        } catch (Exception e) {
            logger.error("Error at image proxy.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{id}/image_files/{level}/{column}_{row}.jpg")
    @GET
    @Produces("image/jpeg")
    public Response getTileImage(@PathParam("id") String id, @PathParam("level") int level, @PathParam("row") int row,
                             @PathParam("column") int column) throws FileNotFoundException {
        try {
            Response callResponse = ClientBuilder
                    .newClient()
                    .target(IMAGESERVE_ENDPOINT + "/data/image/" + id + "/image_files/" + level + "/" + column +"_" + row + ".jpg")
                    .request()
                    .get();
            if(callResponse.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                return Response.ok(callResponse.readEntity(InputStream.class)).build();
            }
            else {
                logger.error("Image proxy did not receive 200: " + callResponse.getStatus());
                return callResponse;
            }
        } catch (Exception e) {
            logger.error("Error at image proxy.", e);
            return Response.serverError().build();
        }
    }
}
