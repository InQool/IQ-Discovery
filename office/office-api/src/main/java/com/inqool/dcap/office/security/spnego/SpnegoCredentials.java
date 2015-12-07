package com.inqool.dcap.office.security.spnego;

import org.picketlink.idm.credential.AbstractBaseCredentials;

/**
 * Distinguishes SPNEGO credentials from all others, so Picketlink can choose the right Credential Handler.
 *
 * @author Matus Zamborsky (inQool)
 */
public class SpnegoCredentials extends AbstractBaseCredentials {
    private String username;

    public SpnegoCredentials(String username) {
        this.username = username;
    }

    @Override
    public void invalidate() {
    }

    public String getUsername() {
        return username;
    }
}
