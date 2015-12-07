package com.inqool.dcap.office.api.config;

import com.inqool.dcap.common.entity.OrganizationSettings;
import com.inqool.dcap.office.api.core.*;
import com.inqool.dcap.office.api.resource.NkListsDbLoaderRS;
import com.inqool.dcap.security.ZdoRoles;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.Map;

/**
* @author Lukas Jane (inQool) 12. 6. 2015.
*/
@Singleton
@Startup
public class DbInit {

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private PicketLinkAccessTmpDeleteThisAfter picketLinkAccessTmpDeleteThisAfter;

    @Inject
    private OrganizationAccess organizationAccess;

    @Inject
    private OrganizationSettingsAccess orgSettingsAccess;

    @Inject
    private NkListsDbLoaderRS nkListsDbLoaderRS;

    @Inject
    private NkListsAccess nkListsAccess;

    @PostConstruct
    public void setup() {

        if(partitionManager.getPartition(Partition.class, "token") == null) {
            Partition partition = new Realm("token");
            partitionManager.add(partition, "token");
        }

//        organizationAccess.resyncAllOrgs();

        //Load nk lists from files to database if they have not been loaded yet
        if(nkListsAccess.listsNotLoaded()) {
            nkListsDbLoaderRS.loadNkListsToDbInner();
        }

    }
}
