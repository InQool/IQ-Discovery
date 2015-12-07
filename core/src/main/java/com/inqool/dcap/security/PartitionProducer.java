package com.inqool.dcap.security;

import org.picketlink.annotations.PicketLink;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.TokenCredential;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Matus Zamborsky (inQool)
 */
@SuppressWarnings("unused")
@ApplicationScoped
public class PartitionProducer {
    @Inject
    private PartitionManager partitionManager;

    @Inject
    private DefaultLoginCredentials credentials;

    @Produces
    @PicketLink
    public Partition getDefaultPartition() {
        if (credentials.getCredential() instanceof TokenCredential) {
            return partitionManager.getPartition(Realm.class, "token");
        }

        return partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);
    }
}