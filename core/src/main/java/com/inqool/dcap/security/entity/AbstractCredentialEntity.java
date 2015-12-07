package com.inqool.dcap.security.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.OwnerReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Cacheable
@MappedSuperclass
public abstract class AbstractCredentialEntity implements Serializable {

    private static final long serialVersionUID = -8032908635337756851L;

    @Id
    @GeneratedValue
    private Long id;

    @OwnerReference
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    private AttributedEntity owner;

    @CredentialClass
    private String typeName;

    @Temporal(TemporalType.TIMESTAMP)
    @EffectiveDate
    private Date effectiveDate;

    @Temporal(TemporalType.TIMESTAMP)
    @ExpiryDate
    private Date expiryDate;

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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}