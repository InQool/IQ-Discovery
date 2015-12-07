package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.dto.PersonInfoHolder;
import com.inqool.dcap.common.dto.UsernamePasswordDto;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.resource.loginlog.LoginLogger;
import com.inqool.dcap.office.security.token.JWSProvider;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import org.picketlink.Identity;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.PartitionManager;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lukas Jane (inQool) 2. 7. 2015.
 */
@Path("/token")
@RequestScoped
public class UserInfoProvider {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private Identity identity;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    @Inject
    private DefaultLoginCredentials defaultLoginCredentials;

    @Inject
    private JWSProvider jwsProvider;

    @Inject
    private LoginLogger loginLogger;

    @Path("/")
    @GET
    @LoggedIn
    @Produces(MediaType.APPLICATION_JSON)
    public PersonInfoHolder getUserInfo() {
        String idmId = ((ZdoUser) identity.getAccount()).getLoginName();
        ZdoUser zdoUser = picketLinkAccess.getUser(idmId);
        ZdoOrganization zdoOrganization = picketLinkAccess.getOrganizationOfUser(zdoUser);

        PersonInfoHolder personInfoHolder = new PersonInfoHolder();
        personInfoHolder.setIdmId(zdoUser.getIdmNumber());
        personInfoHolder.setIdmUsername(zdoUser.getLoginName());
        personInfoHolder.setFirstName(zdoUser.getFirstName());
        personInfoHolder.setLastName(zdoUser.getLastName());
        personInfoHolder.setEmail(zdoUser.getEmail());
        personInfoHolder.setOrganization(zdoOrganization.getName());
        personInfoHolder.setOrganizationName(zdoOrganization.getDisplayName());
        personInfoHolder.getRoles().addAll(picketLinkAccess.getRolesOfUser(zdoUser));
        loginLogger.logLogin(personInfoHolder);
        return personInfoHolder;
    }

    @Path("/login/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UsernamePasswordDto usernamePassword) {
        try {
            //Disallow login on empty passwords - only mojeId login can be with null password
            if(usernamePassword.getPassword() == null || usernamePassword.getPassword().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Login user
            defaultLoginCredentials.setUserId(usernamePassword.getUsername());
            defaultLoginCredentials.setPassword(usernamePassword.getPassword());
            identity.login();
            if(!identity.isLoggedIn()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Return user data
//            String idmId = ((ZdoUser) identity.getAccount()).getLoginName();
            ZdoUser zdoUser = picketLinkAccess.getUser(usernamePassword.getUsername());
            ZdoOrganization zdoOrganization = picketLinkAccess.getOrganizationOfUser(zdoUser);

            PersonInfoHolder personInfoHolder = new PersonInfoHolder();
            personInfoHolder.setIdmId(zdoUser.getIdmNumber());
            personInfoHolder.setIdmUsername(zdoUser.getLoginName());
            personInfoHolder.setFirstName(zdoUser.getFirstName());
            personInfoHolder.setLastName(zdoUser.getLastName());
            personInfoHolder.setEmail(zdoUser.getEmail());
            personInfoHolder.setOrganization(zdoOrganization.getName());
            personInfoHolder.setOrganizationName(zdoOrganization.getDisplayName());
            personInfoHolder.getRoles().addAll(picketLinkAccess.getRolesOfUser(zdoUser));

            //Also generate token
            String token = jwsProvider.generateToken(zdoUser);

            return Response.ok(personInfoHolder).header("authctoken", token).build();
        } catch (Exception e) {
            logger.error("User login failed.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
