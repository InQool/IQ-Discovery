package com.inqool.dcap.security.entity;

import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

@IdentityManaged (org.picketlink.idm.model.Relationship.class)
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_relationship")
public class RelationshipEntity extends AttributedEntity {

    private static final long serialVersionUID = -3619372498444894118L;

    @RelationshipClass
    private String typeName;

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}