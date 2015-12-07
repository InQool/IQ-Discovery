package com.inqool.dcap.security.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.QueryParameter;

import java.time.LocalDateTime;

import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.USER;

/**
 * @author Lukas Jane (inQool) 29. 1. 2015.
 */
@Getter
@Setter
@IdentityStereotype(USER)
public class DiscoveryUser extends User {
    @AttributeProperty
    private String street;
    @AttributeProperty
    private String streetNumber;
    @AttributeProperty
    private String city;
    @AttributeProperty
    private String zip;
    @AttributeProperty
    private String favouriteOrganization; //- default null

    @AttributeProperty
    private String opNumber;

    @AttributeProperty
    private String openidId; //- sparovanie s uctom openId

    @AttributeProperty
    private boolean verified = false;
    @AttributeProperty
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime verifiedDate; //- default null, pridat pri overeni
/*    @AttributeProperty
    private String verification_token; //- generovat pri registracii*/

    private String pwdResetKey; // key that can be used to set a new password, sent to user's email

    public static final QueryParameter OPENID_ID = QUERY_ATTRIBUTE.byName("openidId");
    public static final QueryParameter PWD_RESET_KEY = QUERY_ATTRIBUTE.byName("pwdResetKey");
    /*public static final QueryParameter VERIFICATION_TOKEN = QUERY_ATTRIBUTE.byName("verification_token");*/
}
