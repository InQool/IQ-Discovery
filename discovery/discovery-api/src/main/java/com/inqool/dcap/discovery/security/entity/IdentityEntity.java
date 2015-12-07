package com.inqool.dcap.discovery.security.entity;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.IdentityType;

import javax.persistence.*;
import java.util.Date;

@IdentityManaged (IdentityType.class)
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_discovery_identity")
@Inheritance(strategy = InheritanceType.JOINED)
public class IdentityEntity extends AttributedEntity {

    private static final long serialVersionUID = -6533395974259723600L;

    @IdentityClass
    private String typeName;

    @Temporal(TemporalType.TIMESTAMP)
    @AttributeValue
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @AttributeValue
    private Date expirationDate;

    @AttributeValue
    private boolean enabled;

    @OwnerReference
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    private PartitionEntity partition;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PartitionEntity getPartition() {
        return partition;
    }

    public void setPartition(PartitionEntity partition) {
        this.partition = partition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityEntity other = (IdentityEntity) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId())
                && getTypeName() != null && other.getTypeName() != null && getTypeName().equals(other.getTypeName());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}