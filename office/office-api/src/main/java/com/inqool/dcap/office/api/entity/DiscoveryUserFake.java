package com.inqool.dcap.office.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inqool.dcap.security.entity.IdentityEntity;
import com.inqool.dcap.security.entity.PartitionEntity;
import com.inqool.dcap.security.model.DiscoveryUser;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Lukas Jane (inQool) 18. 8. 2015.
 */
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_discovery_account")
public class DiscoveryUserFake {
    @Id
    @GeneratedValue
    @NotNull
    @JsonIgnore
    private String id;

    @JsonIgnore
    private String loginName;   //= email - not null
    private String firstName;
    private String lastName;
    @JsonIgnore
    private String email;
    private String street;
    private String streetNumber;
    private String city;
    private String zip;
    @JsonIgnore
    private String favouriteOrganization; //- default null
    private String opNumber;
    @JsonIgnore
    private boolean verified = false;
    @JsonIgnore
    private LocalDateTime verifiedDate; //- default null, pridat pri overeni
    /*private String verificationToken; //- generovat pri registracii*/
    @JsonIgnore
    private String openidId; //- sparovanie s uctom openId


/*    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date createdDate;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date expirationDate;
    @JsonIgnore
    private boolean enabled;
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    @JsonIgnore
    private PartitionEntity partition;*/
}
