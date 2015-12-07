package com.inqool.dcap.security.model;

import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.QueryParameter;

import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.USER;

/**
 * @author Lukas Jane (inQool) 29. 1. 2015.
 */
@IdentityStereotype(USER)
public class ZdoUser extends User {

    /**
     * A query parameter used to set the idm number value.
     */
    public static final QueryParameter IDM_NUMBER = QUERY_ATTRIBUTE.byName("idmNumber");

    @AttributeProperty
    private String idmNumber;

    public String getIdmNumber() {
        return idmNumber;
    }

    public void setIdmNumber(String idmId) {
        this.idmNumber = idmId;
    }
}
