package com.inqool.dcap.office.security.token;

import com.inqool.dcap.office.security.spnego.SpnegoAuthScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.TokenAuthenticationScheme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Matus Zamborsky (inQool)
 */
@ApplicationScoped
public class TokenAuthScheme extends TokenAuthenticationScheme {
    @Inject
    private SpnegoAuthScheme spnegoAuthScheme;

//    @Inject
//    private ProjectStage projectStage;

    @Override
    protected String extractTokenFromRequest(HttpServletRequest request) {
        String token = super.extractTokenFromRequest(request);

        // try to extract token from url if not found in header
        if (token == null) {
            token = request.getParameter("authctoken");
        }

        return token;
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        String extractedToken = extractTokenFromRequest(request);

        if (extractedToken != null) {
            creds.setCredential(createCredential(extractedToken));
        } else {
            // we try to extract the credentials using the primary authentication scheme
            getPrimaryAuthenticationScheme().extractCredential(request, creds);
        }
    }

    @Override
    protected void writeToken(String issuedToken, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("authctoken", issuedToken);
    }

    @Override
    protected HttpAuthenticationScheme getPrimaryAuthenticationScheme() {
        return this.spnegoAuthScheme;
    }
}
