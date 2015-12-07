package com.inqool.dcap.security.ldap;

import com.unboundid.ldap.sdk.*;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

/**
 * @author Lukas Jane (inQool) 19. 10. 2015.
 */
public class LdapCredentialHandler<S extends CredentialStore<?>, V extends UsernamePasswordCredentials, U extends Password> extends PasswordCredentialHandler<S, V, U> {

    @Override
    protected boolean validateCredential(IdentityContext context, final CredentialStorage storage, final V credentials, S store) {
        String endpoint = ConfigResolver.getProjectStageAwarePropertyValue("ad.endpoint");
        Integer port = Integer.valueOf(ConfigResolver.getProjectStageAwarePropertyValue("ad.port"));
        String dn = ConfigResolver.getProjectStageAwarePropertyValue("ad.dn");
        String pwd = ConfigResolver.getProjectStageAwarePropertyValue("ad.pwd");
        String filterString = ConfigResolver.getProjectStageAwarePropertyValue("ad.filter");
        String usersDn = ConfigResolver.getProjectStageAwarePropertyValue("ad.users.dn");
        String orgDn = ConfigResolver.getProjectStageAwarePropertyValue("ad.org.dn");
        String krajDn = ConfigResolver.getProjectStageAwarePropertyValue("ad.kraj.dn");

        LDAPConnection connection = null;
        LDAPConnection searchConnection = null;
        Boolean result = false;

        try {
            Filter filter = Filter.create(filterString.replace("{USERNAME}", credentials.getUsername()));

            SearchRequest searchRequest = new SearchRequest(orgDn, SearchScope.SUB, filter, "dn");
            searchConnection = createSearchConnection(endpoint, port, dn, pwd);
            SearchResult searchResult = searchConnection.search(searchRequest);

            if (searchResult.getEntryCount() != 1) {
                searchRequest = new SearchRequest(krajDn, SearchScope.SUB, filter, "dn");
                searchResult = searchConnection.search(searchRequest);
            }

            if (searchResult.getEntryCount() == 1) {
                SearchResultEntry entry = searchResult.getSearchEntries().get(0);

                //Determine if given user is in ZDO users AD group
                boolean isUser = false;
                SearchResultEntry usersNode = searchConnection.getEntry(usersDn);
                for (String member : usersNode.getAttributeValues("member")) {
                    if(entry.getDN().toLowerCase().equals(member.toLowerCase())) {
                        isUser = true;
                        break;
                    }
                }
                if(!isUser) {
                    System.out.println("Given user is not in a ZDO users AD group");
                    return false;
                }

                //Verify password
                connection = new LDAPConnection();
                connection.connect(endpoint, port);
                if (connection.bind(entry.getDN(), new String(credentials.getPassword().getValue())).getResultCode() == ResultCode.SUCCESS) {
                    result = true;
                }
            }
        } catch (LDAPException ex) {
            result = false;
        } finally {
            if (searchConnection != null) {
                searchConnection.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
        return result;
    }

    private LDAPConnection createSearchConnection(String hostname, int port, String bindDN, String bindPassword) throws LDAPException{

        LDAPConnection searchConnection = new LDAPConnection(hostname, port);
        if (bindDN != null) {
            BindResult result = searchConnection.bind(bindDN, bindPassword);

            if (result.getResultCode() != ResultCode.SUCCESS) {
                throw new SecurityException("Failed to authenticate connection to ldap.");
            }
        }

        return searchConnection;
    }
}