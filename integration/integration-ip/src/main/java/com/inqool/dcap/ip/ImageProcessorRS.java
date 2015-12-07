package com.inqool.dcap.ip;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 22. 5. 2015.
 */
@Path("/process/")
public class ImageProcessorRS {
    @Inject
    private ImageProcessor imageProcessor;

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void process(List<String> ids) {
        imageProcessor.processAll(ids);
    }
}