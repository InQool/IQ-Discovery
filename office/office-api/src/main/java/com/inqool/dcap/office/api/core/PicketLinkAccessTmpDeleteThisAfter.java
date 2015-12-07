package com.inqool.dcap.office.api.core;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.security.ZdoBasicModel;
import com.inqool.dcap.security.ZdoRoles;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.*;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 2. 2. 2015.
 */
@ApplicationScoped
public class PicketLinkAccessTmpDeleteThisAfter {

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private com.inqool.dcap.security.PicketLinkAccess picketLinkAccess;

    @Inject
    @Zdo
    private Logger logger;

    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    public PicketLinkAccessTmpDeleteThisAfter() {
    }
    @Inject
    public PicketLinkAccessTmpDeleteThisAfter(PartitionManager partitionManager) {
        this.identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
    }

    public void addPerson(PersonInfoHolderTmpDeleteAlso person) {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));

        ZdoUser newUser = new ZdoUser();
        newUser.setIdmNumber(person.getIdmUsername());
        newUser.setEmail(person.getEmail());
        newUser.setFirstName(person.getFirstName());
        newUser.setLastName(person.getLastName());
        newUser.setLoginName(person.getIdmId());
        identityManager.add(newUser);

        //Add organization/group if needed
        ZdoOrganization org = findOrg(person.getOrganization());
        if(org == null) {
            org = new ZdoOrganization(person.getOrganization(), person.getOrganizationName(), true);
            identityManager.add(org);
        }
        ZdoBasicModel.addToGroup(relationshipManager, newUser, org);

        //Grant roles
        for(ZdoRoles role : person.getRoles()) {
            Role plRole = ZdoBasicModel.getRole(identityManager, role.name());
            ZdoBasicModel.grantRole(relationshipManager, newUser, plRole);
        }
    }

    public void addRolesToPerson(Collection<ZdoRoles> roles, String person) {
        User user = picketLinkAccess.getUser(person);
        grantRoles(roles, user);
    }

    public void grantRoles(Collection<ZdoRoles> roles, User user) {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
        for(ZdoRoles role : roles) {
            Role plRole = ZdoBasicModel.getRole(identityManager, role.name());
            ZdoBasicModel.grantRole(relationshipManager, user, plRole);
        }
    }

    public void removePerson(String personName) {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
        User user = picketLinkAccess.getUser(personName);
        identityManager.remove(user);
    }

    public void revokePersonRole(String personName, ZdoRoles role) {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
        User plUser = picketLinkAccess.getUser(personName);
        Role plRole = ZdoBasicModel.getRole(identityManager, role.name());
        ZdoBasicModel.revokeRole(relationshipManager, plUser, plRole);
    }

    public Map<String, PersonInfoHolderTmpDeleteAlso> reconstructModelFromPicketlink() {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
        Map<String, PersonInfoHolderTmpDeleteAlso> result = new HashMap<>();
        List<ZdoUser> plUsers = identityManager.getQueryBuilder().createIdentityQuery(ZdoUser.class).getResultList();
        for(ZdoUser plUser : plUsers) {
            PersonInfoHolderTmpDeleteAlso oldPerson = new PersonInfoHolderTmpDeleteAlso();
            oldPerson.setIdmUsername(plUser.getLoginName());
            oldPerson.setIdmId(plUser.getIdmNumber());

            //Find his organization (group)
            RelationshipQuery<GroupMembership> groupQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);
            groupQuery.setParameter(GroupMembership.MEMBER, plUser);
            List<GroupMembership> groupMembershipList = groupQuery.getResultList();
            if(groupMembershipList.size() < 1) {
                logger.error("Found user that has no group assigned.");
                continue;
            }
            if(groupMembershipList.get(0).getGroup() == null) continue;
            oldPerson.setOrganization(groupMembershipList.get(0).getGroup().getName());

            //Find his roles
            RelationshipQuery<Grant> rolesQuery = relationshipManager.createRelationshipQuery(Grant.class);
            rolesQuery.setParameter(Grant.ASSIGNEE, plUser);
            List<Grant> grantList = rolesQuery.getResultList();
            for (Grant grant : grantList) {
                if(grant.getRole() == null) continue;
                oldPerson.getRoles().add(ZdoRoles.valueOf(grant.getRole().getName()));
            }
            result.put(plUser.getLoginName(), oldPerson);
        }
        return result;
    }

    public Map<String, PersonInfoHolderTmpDeleteAlso> reconstructSingleOrgModelFromPicketlink(String orgId) {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
        Map<String, PersonInfoHolderTmpDeleteAlso> result = new HashMap<>();

        //Find given organization
        IdentityQueryBuilder identityQueryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> organizationList = identityQueryBuilder.createIdentityQuery(ZdoOrganization.class)
                .where(identityQueryBuilder.equal(ZdoOrganization.NAME, orgId))
                .getResultList();
        if(organizationList.size() != 1) {
            throw new RuntimeException("Organization not found or found multiple times.");
        }
        ZdoOrganization org = organizationList.get(0);

        //Find its users
        RelationshipQuery<GroupMembership> groupQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);
        groupQuery.setParameter(GroupMembership.GROUP, org);
        List<GroupMembership> groupMembershipList = groupQuery.getResultList();

        //Put the users into result
        for(GroupMembership groupMembership : groupMembershipList) {
            ZdoUser plUser = (ZdoUser) groupMembership.getMember();

            PersonInfoHolderTmpDeleteAlso oldPerson = new PersonInfoHolderTmpDeleteAlso();
            oldPerson.setIdmId(plUser.getLoginName());
            oldPerson.setOrganization(orgId);

            //Find also users roles
            RelationshipQuery<Grant> rolesQuery = relationshipManager.createRelationshipQuery(Grant.class);
            rolesQuery.setParameter(Grant.ASSIGNEE, plUser);
            List<Grant> grantList = rolesQuery.getResultList();
            for (Grant grant : grantList) {
                oldPerson.getRoles().add(ZdoRoles.valueOf(grant.getRole().getName()));
            }
            result.put(plUser.getLoginName(), oldPerson);
        }
        return result;
    }

    public ZdoOrganization findOrg(String nameId) {
        identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> orgs = queryBuilder.createIdentityQuery(ZdoOrganization.class)
                .where(queryBuilder.equal(Group.NAME, nameId))
                .getResultList();
        if(orgs.size() > 1) throw new RuntimeException("Found more than 1 organization with given id.");
        if(orgs.isEmpty()) return null;
        return orgs.get(0);
    }
}
