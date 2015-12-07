package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.KeyValueAccess;
import com.inqool.dcap.common.entity.KeyValue;
import com.inqool.dcap.config.Zdo;
import org.picketlink.authorization.annotations.LoggedIn;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/var/")
@LoggedIn
@ApplicationScoped
public class KeyValueRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private KeyValueAccess keyValueAccess;

    @Path("/kdrLoadProgress")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getKdrLoadProgress() {
        try {
            String remainingStr = keyValueAccess.get(KeyValue.kdrDocsToLoadRemaining);
            int remaining = -1;
            if(remainingStr != null) {
                remaining = Integer.valueOf(remainingStr);
            }
            return Response.ok(remaining).build();
        } catch (Exception e) {
            logger.error("Failed while getting kdr load progress.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
