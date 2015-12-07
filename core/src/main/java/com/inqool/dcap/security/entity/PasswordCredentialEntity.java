package com.inqool.dcap.security.entity;

import org.hibernate.annotations.BatchSize;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is a fake entity, so JPAIdentityStore thinks that we support storage of password credentials.
 *
 */
@ManagedCredential (EncodedPasswordStorage.class)
@Cacheable
@BatchSize(size = 100)
@Entity
@Table(name = "dcap_credential_password")
public class PasswordCredentialEntity extends AbstractCredentialEntity {

    private static final long serialVersionUID = -1073369096518010570L;

    @CredentialProperty (name = "encodedHash")
    private String passwordEncodedHash;

    @CredentialProperty (name = "salt")
    private String passwordSalt;

    public String getPasswordEncodedHash() {
        return passwordEncodedHash;
    }

    public void setPasswordEncodedHash(String passwordEncodedHash) {
        this.passwordEncodedHash = passwordEncodedHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

}