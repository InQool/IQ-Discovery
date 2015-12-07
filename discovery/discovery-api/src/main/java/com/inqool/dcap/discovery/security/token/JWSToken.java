package com.inqool.dcap.discovery.security.token;

import org.picketlink.idm.credential.AbstractToken;
import org.picketlink.json.jose.JWS;
import org.picketlink.json.jose.JWSBuilder;

import java.util.List;

public class JWSToken extends AbstractToken {

    private final JWS jws;

    public JWSToken(String encodedToken) {
        super(encodedToken);
        this.jws = new JWSBuilder().build(encodedToken);
    }

    @Override
    public String getSubject() {
        return this.jws.getSubject();
    }

    public List<String> getRoles() {
        return this.jws.getClaimValues("role");
    }

    public List<String> getGroups() {
        return this.jws.getClaimValues("group");
    }

    public List<String> getPermissions() {
        return this.jws.getClaimValues("permission");
    }

    public String getPartition() {
        return this.jws.getIssuer();
    }

    public JWS getJws() {
        return jws;
    }
}
