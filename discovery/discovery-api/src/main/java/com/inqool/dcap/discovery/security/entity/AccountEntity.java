package com.inqool.dcap.discovery.security.entity;

import com.inqool.dcap.security.model.DiscoveryUser;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.annotation.AttributeProperty;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 10. 2. 2015.
 */
@IdentityManaged({DiscoveryUser.class})
@Getter
@Setter
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_discovery_account")
public class AccountEntity extends IdentityEntity {

    private static final long serialVersionUID = -6121193632745759231L;

    @AttributeValue
    private String loginName;   //= email - not null

    @AttributeValue
    private String firstName;

    @AttributeValue
    private String lastName;

    @AttributeValue
    private String email;

    @AttributeValue
    private String street;
    @AttributeValue
    private String streetNumber;
    @AttributeValue
    private String city;
    @AttributeValue
    private String zip;
    @AttributeValue
    private String favouriteOrganization; //- default null

    @AttributeValue
    private String opNumber;

    @AttributeValue
    private boolean verified = false;
    @AttributeValue
    private LocalDateTime verifiedDate; //- default null, pridat pri overeni
/*    @AttributeValue
    private String verificationToken; //- generovat pri registracii*/

    @AttributeValue
    private String openidId; //- sparovanie s uctom openId

    @AttributeValue
    private String pwdResetKey; // key that can be used to set a new password, sent to user's email

}

