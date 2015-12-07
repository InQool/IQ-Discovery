package com.inqool.dcap.security.entity;

import com.inqool.dcap.security.model.ZdoUser;
import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Lukas Jane (inQool) 10. 2. 2015.
 */
@IdentityManaged({ZdoUser.class})
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_account")
public class AccountEntity extends IdentityEntity {

    private static final long serialVersionUID = -6121193632745759231L;

    @AttributeValue
    private String idmNumber;

    @AttributeValue
    private String loginName;

    @AttributeValue
    private String firstName;

    @AttributeValue
    private String lastName;

    @AttributeValue
    private String email;

    public String getIdmNumber() {
        return idmNumber;
    }

    public void setIdmNumber(String idmId) {
        this.idmNumber = idmId;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

