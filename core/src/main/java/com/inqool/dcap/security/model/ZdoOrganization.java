package com.inqool.dcap.security.model;

import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.basic.Group;

import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.GROUP;

/**
 * @author Matus Zamborsky (inQool)
 */
@IdentityStereotype(GROUP)
public class ZdoOrganization extends Group {

    @AttributeProperty
    private String displayName;

    @AttributeProperty
    private Boolean active;

    public ZdoOrganization() {
        super();
    }

    public ZdoOrganization(String name, String displayName, Boolean active) {
        super(name);
        this.displayName = displayName;
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}