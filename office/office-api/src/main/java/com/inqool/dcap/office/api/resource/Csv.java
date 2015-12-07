package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import org.picketlink.authorization.annotations.LoggedIn;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Path("/csv")
@LoggedIn
@RequestScoped
public class Csv {
    @Inject
    @Zdo
    private Logger logger;

    @Path("/")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response reboundCsv(String data, @QueryParam("fileName") String fileName) {
        try {
            return Response.ok(data)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (Exception e) {
            logger.error("Failed miserably.", e);
            return Response.serverError().build();
        }
    }
}
