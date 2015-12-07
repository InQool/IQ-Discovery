package com.inqool.dcap.discovery.security.entity;

import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.PartitionClass;
import org.picketlink.idm.jpa.annotations.entity.ConfigurationName;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.Partition;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

@IdentityManaged(Partition.class)
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_discovery_partition")
public class PartitionEntity extends AttributedEntity {

    private static final long serialVersionUID = -3619372498444894118L;

    @AttributeValue
    private String name;

    @PartitionClass
    private String typeName;

    @ConfigurationName
    private String configurationName;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }
}
