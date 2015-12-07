package com.inqool.dcap.discovery.security.entity;

import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.Identifier;

import javax.persistence.*;
import java.io.Serializable;

@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_discovery_attributed")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AttributedEntity implements Serializable {

    private static final long serialVersionUID = 4307228478304485446L;

    @Id
    @Identifier
    private String id;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        AttributedEntity other = (AttributedEntity) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

}