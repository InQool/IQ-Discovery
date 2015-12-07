package com.inqool.dcap.office.security;

import com.inqool.dcap.config.CustomProjectStageHolder;
import com.inqool.dcap.office.security.spnego.SpnegoCredentialsHandler;
import com.inqool.dcap.office.security.token.JWSConsumer;
import com.inqool.dcap.office.security.token.TokenAuthScheme;
import com.inqool.dcap.security.entity.*;
import com.inqool.dcap.security.ldap.LdapCredentialHandler;
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

        if(CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) {
            builder
                    .http()
                    .forGroup("public")
                    .unprotected()
                    .allPaths()
                    .authenticateWith()
                    .scheme(TokenAuthScheme.class)
                    .forPath("/token/login/*", "public")    //set some paths as unprotected (does not quite work, kerberos eats it before this)
                    .forPath("/autopublish/*", "public")
                    .forPath("/debug/echo/*", "public")
                    .identity()
                    .stateless();
        }
        else {
            builder
                    .http()
                    .forGroup("public")
                    .unprotected()
                    .allPaths()
                    .authenticateWith()
                    .scheme(TokenAuthScheme.class)
//                .forPath("/data/file/*", "public")    //set some paths as unprotected (does not quite work, kerberos eats it before this)
                    .forPath("/autopublish/*", "public")
                    .forPath("/debug/echo/*", "public")
                    .identity()
                    .stateless();
        }
    }

    public void observeIdentityConfigurationEvent(@Observes IdentityConfigurationEvent event) {
        IdentityConfigurationBuilder builder = event.getConfig();

        if(CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) {
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
                            AccountEntity.class,
                            AttributeEntity.class,
                            GroupEntity.class,
                            IdentityEntity.class,
                            PartitionEntity.class,
                            PasswordCredentialEntity.class,
                            PermissionEntity.class,
                            RelationshipEntity.class,
                            RelationshipIdentityEntity.class,
                            RoleEntity.class,
                            PermissionEntity.class
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
                    .addCredentialHandler(SpnegoCredentialsHandler.class)
                    .supportType(
                            ZdoUser.class,
                            ZdoOrganization.class
                    )
                    .supportAllFeatures()
                    .supportAttributes(false)
                    .supportSelfRelationship(GroupMembership.class)
                    .supportSelfRelationship(Grant.class)
                    .mappedEntity(
                            AccountEntity.class,
                            AttributeEntity.class,
                            GroupEntity.class,
                            IdentityEntity.class,
                            PartitionEntity.class,
                            PasswordCredentialEntity.class,
                            PermissionEntity.class,
                            RelationshipEntity.class,
                            RelationshipIdentityEntity.class,
                            RoleEntity.class,
                            PermissionEntity.class
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
    }

}
