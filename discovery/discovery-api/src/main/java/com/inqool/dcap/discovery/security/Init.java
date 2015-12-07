package com.inqool.dcap.discovery.security;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Lukas Jane (inQool) 27. 1. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
public class Init {

    @Inject
    private PartitionManager partitionManager;

    @PostConstruct
    public void makeSureRolesAreCreated() {
/*        IdentityManager identityManager = this.partitionManager.createIdentityManager(partitionManager.getPartition(Realm.class, "default"));
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();

        if(queryBuilder.createIdentityQuery(Role.class).where(queryBuilder.equal(Role.NAME, DiscoveryRoles.NORMAL)).getResultCount() < 1) {
            //Create roles
            for(DiscoveryRoles role : DiscoveryRoles.values()) {
                identityManager.add(new Role(role.name()));
            }
        }*/

        if(partitionManager.getPartition(Partition.class, "token") == null) {
            Partition partition = new Realm("token");
            partitionManager.add(partition, "token");
        }
    }
}
