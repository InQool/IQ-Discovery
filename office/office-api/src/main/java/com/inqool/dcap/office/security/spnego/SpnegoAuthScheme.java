package com.inqool.dcap.office.security.spnego;

import de.ctrlaltdel.SpnegoAuthenticationMechanism;
import org.picketlink.config.http.AuthenticationSchemeConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.http.internal.PicketLinkHttpServletRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This authorization scheme extract the user from wrapped HttpServletRequest. User is authorized
 * to the wrapped HttpServletRequest by means of security domain.
 *
 * @author Matus Zamborsky (inQool)
 */
@ApplicationScoped
public class SpnegoAuthScheme implements HttpAuthenticationScheme {
    @Override
    public void initialize(AuthenticationSchemeConfiguration config) {
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        PicketLinkHttpServletRequest req = (PicketLinkHttpServletRequest) request;
        HttpServletRequest realRequest = (HttpServletRequest) req.getRequest();

        SpnegoAuthenticationMechanism.SimplePrincipal principal = (SpnegoAuthenticationMechanism.SimplePrincipal) realRequest.getUserPrincipal();

        if (principal != null) {
            creds.setUserId(principal.getName());
            creds.setCredential(new SpnegoCredentials(principal.getName()));
        }
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void onPostAuthentication(HttpServletRequest request, HttpServletResponse response) {

    }
}
