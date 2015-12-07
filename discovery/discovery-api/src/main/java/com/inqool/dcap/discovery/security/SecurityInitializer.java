package com.inqool.dcap.discovery.security;

import com.inqool.dcap.discovery.security.entity.*;
import com.inqool.dcap.discovery.security.token.JWSConsumer;
import com.inqool.dcap.discovery.security.token.TokenAuthScheme;
import com.inqool.dcap.security.ldap.LdapCredentialHandler;
import com.inqool.dcap.security.model.DiscoveryUser;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.IdentityConfigurationEvent;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.encoder.SHAPasswordEncoder;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Matus Zamborsky (inQool)
 */
@ApplicationScoped
public class SecurityInitializer {
    @Inject
    private JWSConsumer tokenConsumer;

    @Inject
    private ProjectStage projectStage;

    public void onInit(@Observes SecurityConfigurationEvent event) {
        SecurityConfigurationBuilder builder = event.getBuilder();

        builder
                .http()
                .allPaths()
                .unprotected()
                .forGroup("auth")
                .authenticateWith()
                .scheme(TokenAuthScheme.class)
                .forPath("/auth/*", "auth")
                .identity()
                .stateless();
    }

    public void observeIdentityConfigurationEvent(@Observes IdentityConfigurationEvent event) {
        IdentityConfigurationBuilder builder = event.getConfig();

        if(false /*CustomProjectStageHolder.StagingSCK.equals(projectStage)*/) {
            builder
                    .named("default")
                    .stores()
                    .jpa()
                    .addCredentialHandler(LdapCredentialHandler.class)
                    .setCredentialHandlerProperty(PasswordCredentialHandler.PASSWORD_ENCODER, new SHAPasswordEncoder(256))
                    .supportType(
                            ZdoUser.class,
                            ZdoOrganization.class
                    )
                    .supportAllFeatures()
                    .supportAttributes(false)
                    .supportSelfRelationship(GroupMembership.class)
                    .supportSelfRelationship(Grant.class)
                    .mappedEntity(
                            com.inqool.dcap.security.entity.AccountEntity.class,
                            com.inqool.dcap.security.entity.AttributeEntity.class,
                            com.inqool.dcap.security.entity.GroupEntity.class,
                            com.inqool.dcap.security.entity.IdentityEntity.class,
                            com.inqool.dcap.security.entity.PartitionEntity.class,
                            com.inqool.dcap.security.entity.PasswordCredentialEntity.class,
                            com.inqool.dcap.security.entity.PermissionEntity.class,
                            com.inqool.dcap.security.entity.RelationshipEntity.class,
                            com.inqool.dcap.security.entity.RelationshipIdentityEntity.class,
                            com.inqool.dcap.security.entity.RoleEntity.class,
                            com.inqool.dcap.security.entity.PermissionEntity.class
                    )
                    .named("token")
                    .stores()
                    .token()
                    .tokenConsumer(tokenConsumer)
                    .supportType(
                            ZdoUser.class,
                            ZdoOrganization.class
                    )
                    .unsupportType(Agent.class)
                    .unsupportType(User.class)
                    .supportType(IdentityType.class, Role.class, Group.class, Partition.class, Realm.class)
                    .supportCredentials(true)
                    .supportGlobalRelationship(Relationship.class)
                    .supportGlobalRelationship(Grant.class)
                    .supportGlobalRelationship(GroupMembership.class)
                    .supportGlobalRelationship(GroupRole.class)
                    .supportAttributes(true)
                    .supportPermissions(true);
        }
        else {
            builder
                    .named("default")
                    .stores()
                    .jpa()
                    .supportType(
                            DiscoveryUser.class
                    )
                    .unsupportType(Agent.class)
                    .unsupportType(User.class)
                /*.supportAllFeatures()*/
                    .supportType(IdentityType.class, /*Role.class, Group.class,*/ Partition.class, Realm.class)
                    .supportCredentials(true)
                    .supportAttributes(false)
/*                .supportSelfRelationship(GroupMembership.class)
                .supportSelfRelationship(Grant.class)*/
                    .mappedEntity(
                            AccountEntity.class,
                            AttributeEntity.class,
                        /*GroupEntity.class,*/
                            IdentityEntity.class,
                            PartitionEntity.class,
                            PasswordCredentialEntity.class
                        /*RelationshipEntity.class,
                        RelationshipIdentityEntity.class,
                        RoleEntity.class*//*,
                        PermissionEntity.class*/
                    )
                    .named("token")
                    .stores()
                    .token()
                    .supportType(
                            DiscoveryUser.class
                    )
                    .unsupportType(Agent.class)
                    .unsupportType(User.class)
                    .tokenConsumer(tokenConsumer)
                    .supportType(IdentityType.class, /*Role.class, Group.class,*/ Partition.class, Realm.class)
                    .supportCredentials(true)
                            //NO GLOBAL RELATIONSHIPS OR IT CRASHES ON USER REMOVE
/*                .supportGlobalRelationship(Relationship.class)
                .supportGlobalRelationship(Grant.class)
                .supportGlobalRelationship(GroupMembership.class)
                .supportGlobalRelationship(GroupRole.class)*/
                    .supportAttributes(true);
                /*.supportPermissions(true);*/
        }
    }
}
