package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/feed")
@RequestScoped
public class FeedRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "feed.folder")
    private String FEED_FOLDER;

    @Path("/rss")
    @GET
    @Produces("application/rss+xml")
    public Response feedRss() {
        try {
            File rssFile = new File(FEED_FOLDER + "/rss.xml");
            return Response.ok(rssFile).build();
        } catch (Exception e) {
            logger.error("Failed while getting rss.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/atom")
    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response feedAtom() {
        try {
            File atomFile = new File(FEED_FOLDER + "/atom.xml");
            return Response.ok(atomFile).build();
        } catch (Exception e) {
            logger.error("Failed while getting atom.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
