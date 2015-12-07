package com.inqool.dcap.security.entity;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.jpa.annotations.RelationshipMember;

import javax.persistence.*;
import java.io.Serializable;

@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_identity_relationship")
public class RelationshipIdentityEntity implements Serializable {

    private static final long serialVersionUID = -3619372498444894118L;

    @Id
    @GeneratedValue
    private Long identifier;

    @RelationshipDescriptor
    private String descriptor;

    @RelationshipMember
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    private IdentityEntity identityType;

    @OwnerReference
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    private RelationshipEntity owner;

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public IdentityEntity getIdentityType() {
        return identityType;
    }

    public void setIdentityType(IdentityEntity identityType) {
        this.identityType = identityType;
    }

    public RelationshipEntity getOwner() {
        return owner;
    }

    public void setOwner(RelationshipEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        RelationshipIdentityEntity other = (RelationshipIdentityEntity) obj;

        return getIdentifier() != null && other.getIdentifier() != null && getIdentifier().equals(other.getIdentifier());
    }

    @Override
    public int hashCode() {
        int result = getIdentifier() != null ? getIdentifier().hashCode() : 0;
        result = 31 * result + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
        return result;
    }
}