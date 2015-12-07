package com.inqool.dcap.discovery.security.entity;

import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.basic.Role;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

@IdentityManaged (Role.class)
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_discovery_role")
public class RoleEntity extends IdentityEntity {

    private static final long serialVersionUID = -1111674876657091722L;

    @AttributeValue
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}