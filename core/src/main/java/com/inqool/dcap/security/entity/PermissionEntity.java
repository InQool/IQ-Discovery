package com.inqool.dcap.security.entity;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.PermissionOperation;
import org.picketlink.idm.jpa.annotations.PermissionResourceClass;
import org.picketlink.idm.jpa.annotations.PermissionResourceIdentifier;
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;

import javax.persistence.*;

/**
 * @author Matus Zamborsky (inQool)
 */
@PermissionManaged
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_permission")
public class PermissionEntity {
    @Id
    @GeneratedValue
    private Long id;

    @OwnerReference
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    private AttributedEntity owner;

    @PermissionResourceClass
    private String resourceClass;

    @PermissionResourceIdentifier
    private String resourceIdentifier;

    @PermissionOperation
    private String operation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttributedEntity getOwner() {
        return owner;
    }

    public void setOwner(AttributedEntity owner) {
        this.owner = owner;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
