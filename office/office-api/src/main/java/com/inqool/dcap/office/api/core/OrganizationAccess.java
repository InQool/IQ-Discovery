package com.inqool.dcap.office.api.core;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.dto.OrganizationDto;
import com.inqool.dcap.office.api.dto.UserDto;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.ZdoRoles;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@RequestScoped
@Transactional
public class OrganizationAccess {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "idm.endpoint")
    private String IDM_ENDPOINT;

    @Inject
    private Identity identity;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    public OrganizationAccess() {
    }
    @Inject
    public OrganizationAccess(PartitionManager partitionManager) {
        this.identityManager = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default"));
    }

    public List<OrganizationDto> listAllOrgs() {
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> orgList = queryBuilder
                .createIdentityQuery(ZdoOrganization.class)
                .getResultList();
        List<OrganizationDto> resultOrgList = new ArrayList<>();
        for(ZdoOrganization org : orgList) {
            OrganizationDto organizationDto = new OrganizationDto();
            organizationDto.setName(org.getDisplayName());
            organizationDto.setId(org.getName());

            //Count members
            int memberCount = 0;
            List<GroupMembership> groupMembershipList = relationshipManager
                    .createRelationshipQuery(GroupMembership.class)
                    .setParameter(GroupMembership.GROUP, org)
                    .getResultList();
            for(GroupMembership groupMembership : groupMembershipList) {
                if(groupMembership.getMember() != null && groupMembership.getGroup() != null) {
                    memberCount++;
                }
            }
            organizationDto.setUserCount(memberCount);

            resultOrgList.add(organizationDto);
        }
        return resultOrgList;
    }

    public OrganizationDto getOrg(String orgId) {
        Set<ZdoRoles> userRoles = picketLinkAccess.getUsersRoles();
        if(orgId == null    //Let org admin continue only if this is his own organization
                || (!userRoles.contains(ZdoRoles.sys_admin)
                        && userRoles.contains(ZdoRoles.org_admin)
                        && !orgId.equals(picketLinkAccess.getUsersOrganization().getName())
                )
            ) {
            throw new RuntimeException("Wrong organization id.");
        }
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> orgList = queryBuilder
                .createIdentityQuery(ZdoOrganization.class)
                .where(queryBuilder.equal(ZdoOrganization.NAME, orgId))
                .getResultList();
        if(orgList.size() != 1) {
            throw new RuntimeException("There was not 1 org with given id.");
        }
        ZdoOrganization org = orgList.get(0);

        OrganizationDto organizationDto = new OrganizationDto();
        organizationDto.setName(org.getDisplayName());
        organizationDto.setId(org.getName());

        //Count members
        int memberCount = 0;
        List<GroupMembership> groupMembershipList = relationshipManager
                .createRelationshipQuery(GroupMembership.class)
                .setParameter(GroupMembership.GROUP, org)
                .getResultList();
        for(GroupMembership groupMembership : groupMembershipList) {
            if(groupMembership.getMember() != null && groupMembership.getGroup() != null) {
                memberCount++;
            }
        }
        organizationDto.setUserCount(memberCount);

        return organizationDto;
    }

    public List<UserDto> listOrgUsers(String orgId) {
        Set<ZdoRoles> userRoles = picketLinkAccess.getUsersRoles();
        if(orgId == null    //Let org admin continue only if this is his own organization
                || (!userRoles.contains(ZdoRoles.sys_admin)
                    && userRoles.contains(ZdoRoles.org_admin)
                    && !orgId.equals(picketLinkAccess.getUsersOrganization().getName())
                )
            ) {
            throw new RuntimeException("Wrong organization id.");
        }
        List<UserDto> userDtoList = new ArrayList<>();

        //First find the org
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<ZdoOrganization> orgs = queryBuilder
                .createIdentityQuery(ZdoOrganization.class)
                .where(queryBuilder.equal(ZdoOrganization.NAME, orgId)).getResultList();
        if(orgs.size() != 1) {
            throw new RuntimeException("Couldn't find the organization.");
        }
        ZdoOrganization org = orgs.get(0);

        //Then find its users
        List<GroupMembership> membershipList =
                relationshipManager
                        .createRelationshipQuery(GroupMembership.class)
                        .setParameter(GroupMembership.GROUP, org)
                        .getResultList();
        for(GroupMembership membership : membershipList) {
            if(membership.getMember() == null) continue;
            ZdoUser user = (ZdoUser) membership.getMember();

            UserDto userDto = new UserDto();
            userDto.setIdmId(user.getLoginName());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setMail(user.getEmail());

            //And their roles
            List<Grant> grantList = relationshipManager.createRelationshipQuery(Grant.class)
                    .setParameter(Grant.ASSIGNEE, user)
                    .getResultList();
            for(Grant grant : grantList) {
                if(grant.getRole() != null) {
                    userDto.getRoles().add(grant.getRole().getName());
                }
            }
            userDtoList.add(userDto);
        }
        return userDtoList;
    }

    public void resyncAllOrgs() {
        Response response = ClientBuilder.newClient()
                .target(IDM_ENDPOINT + "sync/")
                .request()
                .post(null);

        if(!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("error resyncing idm, check server log: idm returned " + response.getStatus());
        }
    }

    public void resyncOrg(String orgId) {
        //Let org admin continue only if this is his own organization
        Set<ZdoRoles> userRoles = picketLinkAccess.getUsersRoles();
        if(!userRoles.contains(ZdoRoles.sys_admin)
                && userRoles.contains(ZdoRoles.org_admin)
                && !orgId.equals(picketLinkAccess.getUsersOrganization().getName())
                ) {
            throw new RuntimeException("Wrong organization id.");
        }

        Response response = ClientBuilder.newClient()
                .target(IDM_ENDPOINT + "sync/" + orgId)
                .request()
                .post(null);

        if(!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("error resyncing idm, check server log: idm returned " + response.getStatus());
        }
    }
}
