package com.inqool.dcap.common.dto;

import com.inqool.dcap.security.ZdoRoles;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lukas Jane (inQool) 29. 1. 2015.
 */
public class PersonInfoHolder {
    private String idmId;
    private String idmUsername;
    private String firstName;
    private String lastName;
    private String email;
    private String organization;
    private String organizationName;
    private Set<ZdoRoles> roles = new HashSet<>();

    public String getIdmId() {
        return idmId;
    }

    public void setIdmId(String idmId) {
        this.idmId = idmId;
    }

    public String getIdmUsername() {
        return idmUsername;
    }

    public void setIdmUsername(String idmUsername) {
        this.idmUsername = idmUsername;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Set<ZdoRoles> getRoles() {
        return roles;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
