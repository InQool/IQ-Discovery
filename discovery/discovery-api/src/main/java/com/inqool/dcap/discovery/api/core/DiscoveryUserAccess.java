package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.dto.DiscoveryUserDto;
import com.inqool.dcap.discovery.api.dto.OldPasswordNewPassword;
import com.inqool.dcap.discovery.api.dto.PwdResetKeyNewPassword;
import com.inqool.dcap.discovery.security.PicketLinkAccessDiscovery;
import com.inqool.dcap.discovery.security.mojeid.ConsumerServlet;
import com.inqool.dcap.discovery.security.token.JWSProvider;
import com.inqool.dcap.security.model.DiscoveryUser;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Partition;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Lukas Jane (inQool) 17. 8. 2015.
 */
@RequestScoped
@Transactional
public class DiscoveryUserAccess {
    @Inject
    @Zdo
    private Logger logger;

    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials defaultLoginCredentials;

    @Inject
    private JWSProvider jwsProvider;

    @Inject
    private PicketLinkAccessDiscovery plAccess;

    @Inject
    private MailNotifier mailNotifier;

    public DiscoveryUserAccess() {
    }
    @Inject
    public DiscoveryUserAccess(PartitionManager partitionManager) {
        this.identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
    }

    public void createUser(DiscoveryUserDto discoveryUserDto) throws IOException {
        DiscoveryUser discoveryUser = plAccess.getUser(discoveryUserDto.getUserName());
        if(discoveryUser != null) {
            throw new RuntimeException("User already exists.");
        }

        if(discoveryUserDto.getPassword().isEmpty()) {
            throw new RuntimeException("Password can't be empty.");
        }

        //Create user
        discoveryUser = new DiscoveryUser();
        discoveryUser.setLoginName(discoveryUserDto.getUserName());
        discoveryUser.setEmail(discoveryUserDto.getUserName());
        discoveryUser.setFirstName(discoveryUserDto.getFirstName());
        discoveryUser.setLastName(discoveryUserDto.getLastName());
        discoveryUser.setCity(discoveryUserDto.getCity());
        discoveryUser.setZip(discoveryUserDto.getZip());
        discoveryUser.setStreet(discoveryUserDto.getStreet());
        discoveryUser.setStreetNumber(discoveryUserDto.getStreetNumber());
        discoveryUser.setOpNumber(discoveryUserDto.getOpNumber());
        identityManager.add(discoveryUser);

        //Add password credential
        discoveryUser = plAccess.getUser(discoveryUserDto.getUserName());
        Password password = new Password(discoveryUserDto.getPassword());
        identityManager.updateCredential(discoveryUser, password);

        mailNotifier.notifyUserRegistered(discoveryUserDto.getFirstName(), discoveryUserDto.getLastName(), discoveryUserDto.getUserName());
    }

    public void updateUser(DiscoveryUserDto discoveryUserDto) {
        String realLogin = ((DiscoveryUser) identity.getAccount()).getLoginName();
        if(!discoveryUserDto.getUserName().equals(realLogin)) {
            throw new RuntimeException("Attempt to modify different user.");
        }
        DiscoveryUser discoveryUser = plAccess.getUser(realLogin);
        if(discoveryUser == null) {
            throw new RuntimeException("No such user.");
        }
        if(discoveryUser.isVerified()) {
            throw new RuntimeException("Verified users cannot modify their personal info.");
        }
        discoveryUser.setFirstName(discoveryUserDto.getFirstName());
        discoveryUser.setLastName(discoveryUserDto.getLastName());
        discoveryUser.setCity(discoveryUserDto.getCity());
        discoveryUser.setZip(discoveryUserDto.getZip());
        discoveryUser.setStreet(discoveryUserDto.getStreet());
        discoveryUser.setStreetNumber(discoveryUserDto.getStreetNumber());
        discoveryUser.setOpNumber(discoveryUserDto.getOpNumber());
        identityManager.update(discoveryUser);
    }

    public void deleteUser() throws IOException {
        DiscoveryUser user = plAccess.getCurrentUser();
        identityManager.remove(user);

        mailNotifier.notifyUserDeleted(user.getFirstName(), user.getLastName(), user.getLoginName());
    }

    public Response login(DiscoveryUserDto discoveryUserDto) {
        //Disallow login on empty passwords - only mojeId login can be with null password
        if(discoveryUserDto.getPassword() == null || discoveryUserDto.getPassword().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //Login user
        defaultLoginCredentials.setUserId(discoveryUserDto.getUserName());
        defaultLoginCredentials.setPassword(discoveryUserDto.getPassword());
        identity.login();
        if(!identity.isLoggedIn()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //Return user data
        DiscoveryUser discoveryUser = (DiscoveryUser) identity.getAccount();
        DiscoveryUserDto result = new DiscoveryUserDto();
        result.setUserName(discoveryUser.getLoginName());
        result.setFirstName(discoveryUser.getFirstName());
        result.setLastName(discoveryUser.getLastName());
        result.setCity(discoveryUser.getCity());
        result.setZip(discoveryUser.getZip());
        result.setStreet(discoveryUser.getStreet());
        result.setStreetNumber(discoveryUser.getStreetNumber());
        result.setOpNumber(discoveryUser.getOpNumber());
        result.setVerified(discoveryUser.isVerified());

        //Also generate token
        String token = jwsProvider.generateToken(identity.getAccount());

        return Response.ok(result).header("authctoken", token).build();
    }

    public Map<String, String> mojeIdLogin(Map<String, String> params) {
        String mojeId = params.get(ConsumerServlet.MOJE_ID);

        DiscoveryUser foundUser = plAccess.getUserByMojeId(mojeId);

        //User validated in mojeid?
        boolean validated = false;
        if("VALIDATED".equals(params.get(ConsumerServlet.STATUS))
                && params.get(ConsumerServlet.VALID) != null
                && params.get(ConsumerServlet.VALID).toLowerCase().equals("true")) {
            validated = true;
        }

        //New user, register him
        if(foundUser == null) {
            foundUser = plAccess.getUser(params.get(ConsumerServlet.EMAIL_PRIMARY));
            if(foundUser != null) {
                Map<String, String> result = new HashMap<>();
                logger.debug("E-mail associated with this account is already registered.");
                result.put("message", "E-mail associated with this account is already registered.");
                return result;
            }

            //Create user
            foundUser = new DiscoveryUser();

            String streetBoth = params.get(ConsumerServlet.STREET);
            if(streetBoth != null && !streetBoth.isEmpty()) {
                foundUser.setStreetNumber(parseStreetNumber(streetBoth));
                foundUser.setStreet(parseStreetName(streetBoth));
            }
            foundUser.setLoginName(params.get(ConsumerServlet.EMAIL_PRIMARY));
            foundUser.setEmail(params.get(ConsumerServlet.EMAIL_PRIMARY));
            foundUser.setFirstName(params.get(ConsumerServlet.FIRST_NAME));
            foundUser.setLastName(params.get(ConsumerServlet.SURNAME));
            foundUser.setCity(params.get(ConsumerServlet.CITY));
            foundUser.setZip(params.get(ConsumerServlet.POSTAL_CODE));
            foundUser.setOpNumber(params.get(ConsumerServlet.OP_NUMBER));
            foundUser.setOpenidId(mojeId);
            foundUser.setVerified(validated);

            identityManager.add(foundUser);

            //Add password credential
            Password password = new Password("");
            identityManager.updateCredential(foundUser, password);
        }
        else {  //Existing user, update info
            boolean doUpdate = false;
            if(!params.get(ConsumerServlet.EMAIL_PRIMARY).equals(foundUser.getLoginName())) {
                doUpdate = true;
                foundUser.setLoginName(params.get(ConsumerServlet.EMAIL_PRIMARY));
                foundUser.setEmail(params.get(ConsumerServlet.EMAIL_PRIMARY));
            }
            if(!params.get(ConsumerServlet.FIRST_NAME).equals(foundUser.getFirstName())) {
                doUpdate = true;
                foundUser.setFirstName(params.get(ConsumerServlet.FIRST_NAME));
            }
            if(!params.get(ConsumerServlet.SURNAME).equals(foundUser.getLastName())) {
                doUpdate = true;
                foundUser.setLastName(params.get(ConsumerServlet.SURNAME));
            }
            if(!params.get(ConsumerServlet.CITY).equals(foundUser.getCity())) {
                doUpdate = true;
                foundUser.setCity(params.get(ConsumerServlet.CITY));
            }
            if(!params.get(ConsumerServlet.POSTAL_CODE).equals(foundUser.getZip())) {
                doUpdate = true;
                foundUser.setZip(params.get(ConsumerServlet.POSTAL_CODE));
            }
            if(params.get(ConsumerServlet.OP_NUMBER) != null && !params.get(ConsumerServlet.OP_NUMBER).equals(foundUser.getOpNumber())) {
                doUpdate = true;
                foundUser.setOpNumber(params.get(ConsumerServlet.OP_NUMBER));
            }
            String streetBoth = params.get(ConsumerServlet.STREET);
            if(streetBoth != null && !streetBoth.isEmpty()) {
                String streetNumber = parseStreetNumber(streetBoth);
                String streetName = parseStreetName(streetBoth);
                if(!streetName.equals(foundUser.getStreet())) {
                    doUpdate = true;
                    foundUser.setStreet(streetName);
                }
                if(!streetNumber.equals(foundUser.getStreetNumber())) {
                    doUpdate = true;
                    foundUser.setStreetNumber(streetNumber);
                }
            }
            if(validated != foundUser.isVerified()) {
                doUpdate = true;
                foundUser.setVerified(validated);
            }
            if(doUpdate) {
                identityManager.update(foundUser);
            }
        }

        //Login user
        defaultLoginCredentials.setUserId(params.get(ConsumerServlet.EMAIL_PRIMARY));
        defaultLoginCredentials.setPassword("");
        identity.login();
        if(!identity.isLoggedIn()) {
            throw new RuntimeException("login failed");
        }

        //Return user data
        Map<String, String> result = new HashMap<>();

        DiscoveryUser discoveryUser = (DiscoveryUser) identity.getAccount();
        result.put("userName", discoveryUser.getLoginName());
        result.put("firstName", discoveryUser.getFirstName());
        result.put("lastName", discoveryUser.getLastName());
        result.put("city", discoveryUser.getCity());
        result.put("zip", discoveryUser.getZip());
        result.put("street", discoveryUser.getStreet());
        result.put("streetNumber", discoveryUser.getStreetNumber());
        result.put("opNumber", discoveryUser.getOpNumber());
        result.put("verified", String.valueOf(discoveryUser.isVerified()));

        result.put("authctoken", jwsProvider.generateToken(discoveryUser));

        return result;
    }

    private String parseStreetName(String street) {
        return street.replaceAll("[0-9/]+", " ").trim();
    }

    private String parseStreetNumber(String street) {
        String streetNumbers = street.replaceAll("[^0-9]+", " ");
        streetNumbers = String.join("/", streetNumbers.trim().split(" "));
        return streetNumbers;
    }

    public boolean updateUserPassword(OldPasswordNewPassword passwords) {
        String oldPswd = passwords.getOldPassword();
        String newPswd = passwords.getNewPassword();
        if(oldPswd == null || oldPswd.isEmpty() || newPswd == null || newPswd.isEmpty()) {
            return false;
        }
        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials();
        DiscoveryUser user = plAccess.getCurrentUser();
        usernamePasswordCredentials.setUsername(user.getLoginName());
        usernamePasswordCredentials.setPassword(new Password(oldPswd));
        identityManager.validateCredentials(usernamePasswordCredentials);

        if(!usernamePasswordCredentials.getStatus().equals(Credentials.Status.VALID)) {
            return false;
        }

        Password password = new Password(newPswd);
        identityManager.updateCredential(user, password);
        return true;
    }

    public Response resetUserPassword(String email) throws IOException {
        DiscoveryUser user = plAccess.getUser(email);
        if(user == null) {
            logger.debug("No user with this email registered.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(user.getOpenidId() != null) {
            logger.error("OpenId users can not reset their password.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String pwdResetKey = UUID.randomUUID().toString();
        user.setPwdResetKey(pwdResetKey);
        identityManager.update(user);

        mailNotifier.notifyPasswordReset(user.getFirstName(), user.getLastName(), user.getLoginName(), pwdResetKey);

        return Response.ok().build();
    }

    public Response checkPwdResetKey(String pwdResetKey) {
        DiscoveryUser user = plAccess.getUserByPwdResetKey(pwdResetKey);
        if(user == null) {
            logger.debug("No user with this pwdResetKey.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok().build();
    }

    public Response setNewPassword(PwdResetKeyNewPassword pwdResetKeyNewPassword) {
        DiscoveryUser user = plAccess.getUserByPwdResetKey(pwdResetKeyNewPassword.getPwdResetKey());
        if(user == null) {
            logger.debug("No user with this pwdResetKey.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Password password = new Password(pwdResetKeyNewPassword.getNewPassword());
        identityManager.updateCredential(user, password);

        user.setPwdResetKey(null);
        identityManager.update(user);
        return Response.ok().build();
    }

/*    public Response generateCode() {
        DiscoveryUser discoveryUser = plAccess.getCurrentUser();
        discoveryUser.setVerification_token(discoveryUser.);
return null;
    }*/
}
