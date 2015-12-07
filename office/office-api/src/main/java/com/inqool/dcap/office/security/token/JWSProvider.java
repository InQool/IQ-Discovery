package com.inqool.dcap.office.security.token;

import com.inqool.dcap.office.api.core.PersonInfoHolderTmpDeleteAlso;
import com.inqool.dcap.office.api.core.PicketLinkAccessTmpDeleteThisAfter;
import com.inqool.dcap.security.model.ZdoUser;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.json.jose.JWSBuilder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestScoped
public class JWSProvider implements Token.Provider<JWSToken> {
    private Integer expiration = 50 * 60;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private RelationshipManager relationshipManager;

    @Inject
    private PermissionManager permissionManager;

    @Inject
    private PicketLinkAccessTmpDeleteThisAfter picketLinkAccessTmpDeleteThisAfter;

    @Inject
    @KeysProvider.PrivateKey
    private byte[] privateKey;

    /*
     * Construct a JWS signed with the private key provided by the partition.
     */
    @Override
    public JWSToken issue(Account account) {
//        createTestTokens();
        String token = generateToken(account);
        return new JWSToken(token);
    }

    public String generateToken(Account account) {
        ZdoUser zdoUser = (ZdoUser) account;
        JWSBuilder builder = new JWSBuilder();
        return builder
                .id(UUID.randomUUID().toString())
                .rsa256(privateKey)
                .issuer("token")        // needs to be token
                .issuedAt(getCurrentTime())
                .subject(zdoUser.getLoginName())
                .expiration(getCurrentTime() + expiration)
                .notBefore(getCurrentTime())
                .claim("group", getGroups(account))
                .claim("role", getRelationships(account))
                        .build()
                        .encode();
//                .claim("permission", getPermissions(account));
    }

    private String[] getPermissions(Account tkAccount) {
        List<Permission> permissions = permissionManager.listPermissions(tkAccount);
        List<Grant> query = relationshipManager.createRelationshipQuery(Grant.class)
                .setParameter(Grant.ASSIGNEE, tkAccount)
                .getResultList();

        List<Role> roles = query.stream()
                .map(Grant::getRole)
                .collect(Collectors.toList());

        roles.stream()
                .map(permissionManager::listPermissions)
                .forEach(permissions::addAll);

        return permissions.stream()
                .map(permission -> String.class.getName() + "[" + permission.getResourceIdentifier() + "]." + permission.getOperation())
                .toArray(String[]::new);
    }

    private String[] getRelationships(Account employee) {
        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);
        query.setParameter(Grant.ASSIGNEE, employee);
        List<Grant> result = query.getResultList();

        List<String> collect = result.stream()
                .filter(group -> group.getRole() != null)
                .map(grant -> grant.getRole().getName())
                .collect(Collectors.toList());

        return collect.toArray(new String[collect.size()]);
    }

    private String[] getGroups(Account employee) {
        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        query.setParameter(GroupMembership.MEMBER, employee);
        List<GroupMembership> result = query.getResultList();

        List<String> collect = result.stream()
                .filter(group -> group.getGroup() != null)
                .map(group -> group.getGroup().getName())
                .collect(Collectors.toList());

        return collect.toArray(new String[collect.size()]);
    }

    /*
     * Return new token. No validation of previous token is done, because user should be already authenticated.
     */
    @Override
    public JWSToken renew(Account account, JWSToken renewToken) {
        return reissue(account, renewToken);
    }

    /*
        NOP. JWS tokens cant be invalidated
     */
    @Override
    public void invalidate(Account account) {
    }

    @Override
    public Class<JWSToken> getTokenType() {
        return JWSToken.class;
    }


    private int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private JWSToken reissue(Account account, JWSToken token) {
        ZdoUser zdoUser = (ZdoUser) account;

        JWSBuilder builder = new JWSBuilder();

        List<String> roles = token.getRoles();
//        List<String> permissions = token.getPermissions();
        List<String> groups = token.getGroups();

        builder
                .id(UUID.randomUUID().toString())
                .rsa256(privateKey)
                .issuer("token")
                .issuedAt(getCurrentTime())
                .subject(zdoUser.getLoginName())
                .expiration(getCurrentTime() + expiration)
                .notBefore(getCurrentTime())
                .claim("group", groups.toArray(new String[groups.size()]))
                .claim("role", roles.toArray(new String[roles.size()]));
//                .claim("permission", permissions.toArray(new String[permissions.size()]));

        return new JWSToken(builder.build().encode());
    }

    public void createTestTokens() {
        expiration = 60*60*24*1000;

        Map<String, PersonInfoHolderTmpDeleteAlso> map = picketLinkAccessTmpDeleteThisAfter.reconstructModelFromPicketlink();
        String rr = "";
        for(PersonInfoHolderTmpDeleteAlso p : map.values()) {

            IdentityQueryBuilder queryBuilder = partitionManager.createIdentityManager(partitionManager.getPartition(Partition.class, "default")).getQueryBuilder();
            List<ZdoUser> agents = queryBuilder.createIdentityQuery(ZdoUser.class)
                    .where(queryBuilder.equal(ZdoUser.LOGIN_NAME, p.getIdmUsername())).getResultList();
            Agent zdoUser = agents.get(0);

            JWSBuilder builder = new JWSBuilder();

            builder
                    .id(UUID.randomUUID().toString())
                    .rsa256(privateKey)
                    .issuer("token")        // needs to be token
                    .issuedAt(getCurrentTime())
                    .subject(p.getIdmUsername())
                    .expiration(getCurrentTime() + expiration)
                    .notBefore(getCurrentTime())
                    .claim("group", getGroups(zdoUser))
                    .claim("role", getRelationships(zdoUser));
//                .claim("permission", getPermissions(account));

            JWSToken x = new JWSToken(builder.build().encode());
            String y = x.getToken();
            rr += p.getIdmUsername() + " : \""+ y + "\",\n";
        }

        System.out.print(rr);
    }
}
