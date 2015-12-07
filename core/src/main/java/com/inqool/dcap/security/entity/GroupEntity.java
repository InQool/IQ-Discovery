package com.inqool.dcap.security.entity;

import com.inqool.dcap.security.model.ZdoOrganization;
import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.basic.Group;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@IdentityManaged ({Group.class, ZdoOrganization.class})
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_group")
public class GroupEntity extends IdentityEntity {

    private static final long serialVersionUID = 158403858486164771L;

    @AttributeValue
    private String name;

    @AttributeValue
    private String path;

    @AttributeValue
    private String displayName;

    @AttributeValue
    private Boolean active;

    @ManyToOne
    @AttributeValue (name = "parentGroup")
    private GroupEntity parent;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public GroupEntity getParent() {
        return parent;
    }

    public void setParent(GroupEntity parent) {
        this.parent = parent;
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