package com.inqool.dcap.security;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 21. 4. 2015.
 */
@ApplicationScoped
public class PicketLinkAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private Identity identity;

    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    public PicketLinkAccess() {
    }
    @Inject
    public PicketLinkAccess(PartitionManager partitionManager) {
        this.identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
    }

    public ZdoUser getUser() {
        return (ZdoUser)identity.getAccount();
    }

    public ZdoUser getUser(String idmId) throws IdentityManagementException {
        if (idmId == null || idmId.isEmpty()) {
            return null;
        }
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoUser> agents = queryBuilder.createIdentityQuery(ZdoUser.class)
                .where(queryBuilder.equal(ZdoUser.LOGIN_NAME, idmId)).getResultList();
        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same IDM_ID.");
        }
    }

    public ZdoOrganization getUsersOrganization() {
        return getOrganizationOfUser((ZdoUser)identity.getAccount());
    }

    public ZdoOrganization getOrganizationOfUser(ZdoUser user) {
        //If this user is from token
        if(user.getIdmNumber() == null) {
            //Fetch real user from database
            user = getUser(user.getLoginName());
        }

        RelationshipQuery<GroupMembership> groupQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);
        groupQuery.setParameter(GroupMembership.MEMBER, user);
        List<GroupMembership> groupMembershipList = groupQuery.getResultList();
        for(GroupMembership groupMembership : groupMembershipList) {
            Group group = groupMembership.getGroup();
            if (group instanceof ZdoOrganization) {
                return (ZdoOrganization) group;
            }
        }
        throw new RuntimeException("No organization found for user");
    }

    public Set<ZdoRoles> getUsersRoles() {
        return getRolesOfUser((ZdoUser)identity.getAccount());
    }

    public Set<ZdoRoles> getRolesOfUser(ZdoUser user) {
        Set<ZdoRoles> resultSet = new HashSet<>();
        //If this user is from token
        if(user.getIdmNumber() == null) {
            //Fetch real user from database
            user = getUser(user.getLoginName());
        }

        RelationshipQuery<Grant> rolesQuery = relationshipManager.createRelationshipQuery(Grant.class);
        rolesQuery.setParameter(Grant.ASSIGNEE, user);
        List<Grant> rolesQueryResultList = rolesQuery.getResultList();
        for(Grant grant : rolesQueryResultList) {
            Role role = grant.getRole();
            if(role == null) continue;
            resultSet.add(ZdoRoles.valueOf(role.getName()));
        }
        return resultSet;
    }

    public List<ZdoUser> getUsersOfOrganization(ZdoOrganization organization) {
        RelationshipQuery<GroupMembership> groupQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);
        groupQuery.setParameter(GroupMembership.GROUP, organization);
        List<GroupMembership> groupMembershipList = groupQuery.getResultList();
        List<ZdoUser> resultList = new ArrayList<>();
        for(GroupMembership groupMembership : groupMembershipList) {
            if(groupMembership.getMember() == null) continue;
            resultList.add((ZdoUser) groupMembership.getMember());
        }
        return resultList;
    }

    public List<ZdoUser> getFellowCurators(String ofUser) {
        List<ZdoUser> orgUsers = getUsersOfOrganization(getOrganizationOfUser(getUser(ofUser)));
        List<ZdoUser> resultList = new ArrayList<>();
        for(ZdoUser orgUser : orgUsers) {
            if(getRolesOfUser(orgUser).contains(ZdoRoles.curator)) {
                resultList.add(orgUser);
            }
        }
        return resultList;
    }

    public List<ZdoOrganization> listOrganizations() {
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> orgs = queryBuilder.createIdentityQuery(ZdoOrganization.class).getResultList();
        return orgs;
    }

    public String getOrganizationName(String idmId) {
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> orgs = queryBuilder
                .createIdentityQuery(ZdoOrganization.class)
                .where(queryBuilder.equal(ZdoOrganization.NAME, idmId))
                .getResultList();
        if(orgs.size() != 1) {
            throw new RuntimeException(orgs.size() + " organizations found for idmId " + idmId + "!");
        }
        return orgs.get(0).getDisplayName();
    }

    public List<ZdoUser> getAllUsers() {
        return identityManager.getQueryBuilder().createIdentityQuery(ZdoUser.class).getResultList();
    }

    public String removePrispevkovaOrganizaceFromOrgName(String orgName) {
        return orgName.replace(", příspěvková organizace", "").trim();
    }
}
