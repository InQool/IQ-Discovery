/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inqool.dcap.office.security.spnego;

import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.handler.AbstractCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.util.Date;

/**
 * This implementation only check if there are credentials. The existence of credentials means that the user was
 * authenticated into security domain and therefore is already authenticated.
 *
 * @author Matus Zamborsky (inQool)
 */
@SupportsCredentials(
        credentialClass = {SpnegoCredentials.class},
        credentialStorage = EncodedPasswordStorage.class)
public class SpnegoCredentialsHandler<S extends CredentialStore<?>, V extends SpnegoCredentials, U extends Password>
        extends AbstractCredentialHandler<S, V, U> {

    @Override
    public void setup(S store) {
        super.setup(store);
    }

    @Override
    protected Account getAccount(final IdentityContext context, final V credentials) {
        return getAccount(context, credentials.getUsername());
    }

    @Override
    protected boolean validateCredential(IdentityContext context, final CredentialStorage storage, final V credentials, S store) {
        return credentials != null && credentials.getUsername() != null;
    }

    @Override
    protected CredentialStorage getCredentialStorage(final IdentityContext context, final Account account, final V credentials, final S store) {
        return null;
    }

    @Override
    public CredentialStorage createCredentialStorage(IdentityContext context, Account account, U password, S store,
                                                     Date effectiveDate, Date expiryDate) {

        return null;
    }
}