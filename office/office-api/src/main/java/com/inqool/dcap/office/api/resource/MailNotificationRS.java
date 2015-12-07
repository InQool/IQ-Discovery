package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.MailNotificationAccess;
import com.inqool.dcap.common.entity.MailNotification;
import com.inqool.dcap.security.ZdoRoles;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/mailNotification")
@LoggedIn
@RolesAllowed(ZdoRoles.ADMIN_SYS)
@RequestScoped
public class MailNotificationRS {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private MailNotificationAccess mailNotificationAccess;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMailNotifications() {
        try {
            List<MailNotification> mailNotificationList = mailNotificationAccess.listMailNotifications();
            return Response.ok(mailNotificationList).build();
        } catch (Exception e) {
            logger.error("Failed while getting mail notification list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewMailNotification(MailNotification mailNotification) {
        try {
            int mailNotificationId = mailNotificationAccess.createMailNotification(mailNotification);
            return Response.ok(mailNotificationId).build();
        } catch (Exception e) {
            logger.error("Failed while creating mail notification.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{mailNotificationId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMailNotification(@PathParam("mailNotificationId") int mailNotificationId) {
        try {
            MailNotification mailNotification = mailNotificationAccess.fetchMailNotification(mailNotificationId);
            return Response.ok(mailNotification).build();
        } catch(Exception e) {
            logger.error("Could not get mail notification.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{mailNotificationId}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMailNotification(MailNotification mailNotification, @PathParam("mailNotificationId") int mailNotificationId) {
        try {
            mailNotificationAccess.updateMailNotification(mailNotificationId, mailNotification);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update mail notification.", e);
            return Response.serverError().build();
        }
    }

    @Path("/delete/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteMailNotifications(List<Integer> mailNotificationIds) {
        try {
            mailNotificationAccess.deleteMailNotifications(mailNotificationIds);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete mail notifications.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{mailNotificationId}/")
    @DELETE
    public Response deleteMailNotification(@PathParam("mailNotificationId") int mailNotificationId) {
        try {
            mailNotificationAccess.deleteMailNotification(mailNotificationId);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete mail notification.", e);
            return Response.serverError().build();
        }
    }
}
