package com.inqool.dcap.image.serving;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author Matus Zamborsky (inQool)
 */
@Path("/data/image")
public class ImageResource {
    @Inject
    @Zdo
    private org.slf4j.Logger logger;

    @Inject
    @ConfigProperty(name = "ip.folder")
    private String ipFolder;

    @Inject
    private DataStore dataStore;

    @Path("/{id}/image.dzi")
    @GET
    @Produces(MediaType.TEXT_XML)
    public Response getDescriptor(@PathParam("id") String id) throws FileNotFoundException {
        try {
            String path = ipFolder + "/" + dataStore.createDeepPath(id) + "/image.dzi";
            File file = new File(path);
            if(!file.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(file).build();
        } catch (Exception e) {
            logger.error("Error at image serve", e);
            return Response.serverError().build();
        }
    }

    @Path("/{id}/image_files/{level}/{column}_{row}.jpg")
    @GET
    @Produces("image/jpeg")
    public File getTileImage(@PathParam("id") String id, @PathParam("level") int level, @PathParam("row") int row,
                                 @PathParam("column") int column) throws FileNotFoundException {

        String path = ipFolder + "/" + dataStore.createDeepPath(id) + "/image_files/" + level + "/" + column + "_" + row + ".jpg";
        return new File(path);
    }
}
