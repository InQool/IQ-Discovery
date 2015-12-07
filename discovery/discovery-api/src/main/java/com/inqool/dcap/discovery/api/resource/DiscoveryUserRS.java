package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.DiscoveryUserAccess;
import com.inqool.dcap.discovery.api.dto.DiscoveryUserDto;
import com.inqool.dcap.discovery.api.dto.OldPasswordNewPassword;
import com.inqool.dcap.discovery.api.dto.PwdResetKeyNewPassword;
import org.picketlink.authorization.annotations.LoggedIn;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/")
@RequestScoped
public class DiscoveryUserRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DiscoveryUserAccess discoveryUserAccess;

    @Path("/user")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(DiscoveryUserDto discoveryUserDto) {
        try {
            discoveryUserAccess.createUser(discoveryUserDto);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while creating new user.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/user/login/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(DiscoveryUserDto discoveryUserDto) {
        try {
            return discoveryUserAccess.login(discoveryUserDto);
        } catch (Exception e) {
            logger.error("User login failed.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @LoggedIn
    @Path("/auth/user/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(DiscoveryUserDto discoveryUserDto) {
        try {
            discoveryUserAccess.updateUser(discoveryUserDto);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while updating user.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @LoggedIn
    @Path("/auth/user/")
    @DELETE
    public Response deleteUser() {
        try {
            discoveryUserAccess.deleteUser();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while deleting user.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @LoggedIn
    @Path("/auth/user/password")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(OldPasswordNewPassword passwords) {
        try {
            boolean success = discoveryUserAccess.updateUserPassword(passwords);
            if (!success) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed while updating user password.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/user/resetPassword")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response resetPassword(String email) {
        try {
            return discoveryUserAccess.resetUserPassword(email);
        } catch (Exception e) {
            logger.error("Failed while resetting user password.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/user/checkPwdResetKey")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response checkPwdResetKey(String pwdResetKey) {
        try {
            return discoveryUserAccess.checkPwdResetKey(pwdResetKey);
        } catch (Exception e) {
            logger.error("Failed while setting checking password reset key.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/user/setNewPassword")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNewPassword(PwdResetKeyNewPassword pwdResetKeyNewPassword) {
        try {
            return discoveryUserAccess.setNewPassword(pwdResetKeyNewPassword);
        } catch (Exception e) {
            logger.error("Failed while setting new user password.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

/*    @LoggedIn
    @Path("/auth/user/generateCode/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateCode() {
        try {
            return discoveryUserAccess.generateCode();
        } catch (Exception e) {
            logger.error("Generating verification code for user failed.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }*/
}
