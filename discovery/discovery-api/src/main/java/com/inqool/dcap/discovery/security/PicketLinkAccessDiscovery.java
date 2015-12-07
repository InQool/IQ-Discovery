package com.inqool.dcap.discovery.security;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.security.model.DiscoveryUser;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 21. 4. 2015.
 */
@ApplicationScoped
public class PicketLinkAccessDiscovery {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private Identity identity;

    private IdentityManager identityManager;

    public PicketLinkAccessDiscovery() {
    }
    @Inject
    public PicketLinkAccessDiscovery(PartitionManager partitionManager) {
        this.identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
    }

    public DiscoveryUser getCurrentUser() {
        return getUser(((DiscoveryUser) identity.getAccount()).getLoginName());
    }

    public DiscoveryUser getUser(String login) {
        if (login == null || login.isEmpty()) {
            return null;
        }
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<DiscoveryUser> agents = queryBuilder.createIdentityQuery(DiscoveryUser.class)
                .where(queryBuilder.equal(DiscoveryUser.LOGIN_NAME, login)).getResultList();
        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same IDM_ID.");
        }
    }

    public DiscoveryUser getUserByMojeId(String mojeId) {
        if (mojeId == null || mojeId.isEmpty()) {
            return null;
        }
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<DiscoveryUser> agents = queryBuilder.createIdentityQuery(DiscoveryUser.class)
                .where(queryBuilder.equal(DiscoveryUser.OPENID_ID, mojeId)).getResultList();
        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same OPENID_ID.");
        }
    }

    public DiscoveryUser getUserByPwdResetKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<DiscoveryUser> agents = queryBuilder.createIdentityQuery(DiscoveryUser.class)
                .where(queryBuilder.equal(DiscoveryUser.PWD_RESET_KEY, key)).getResultList();
        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same PWD_RESET_KEY.");
        }
    }
}
