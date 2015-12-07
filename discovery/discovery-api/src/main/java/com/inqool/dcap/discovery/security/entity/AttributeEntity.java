package com.inqool.dcap.discovery.security.entity;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;

import javax.persistence.*;
import java.io.Serializable;

@BatchSize(size = 100)
@Cacheable
@Entity
@Table(name = "dcap_discovery_attribute")
public class AttributeEntity implements Serializable {

    private static final long serialVersionUID = 5255050503622214581L;

    @Id
    @GeneratedValue
    private Long id;

    @OwnerReference
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    private AttributedEntity owner;

    @AttributeClass
    private String typeName;

    @AttributeName
    private String name;

    @AttributeValue
    private String value;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        AttributeEntity other = (AttributeEntity) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

}
