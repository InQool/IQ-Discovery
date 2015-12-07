package com.inqool.dcap.office.security.token;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.reflection.Reflections;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.permission.Permission;
import org.picketlink.json.jose.JWS;
import org.picketlink.json.jose.JWSBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

@ApplicationScoped
public class JWSConsumer implements Token.Consumer<JWSToken> {
    @Inject
    private PartitionManager partitionManager;

    @Inject
    @KeysProvider.PrivateKey
    private byte[] privateKey;

    protected String extractSubject(JWSToken token) {
        return token.getSubject();
    }

    protected Set<String> extractRoles(JWSToken token) {
        return new LinkedHashSet<>(token.getRoles());
    }

    protected Set<String> extractGroups(JWSToken token) {
        return new LinkedHashSet<>(token.getGroups());
    }

    protected String extractPartition(JWSToken token) {
        return token.getPartition();
    }


    @Override
    public boolean validate(JWSToken token) {
        return validateExpire(token) && validateNotBefore(token) && validateSignature(token);
    }

    @Override
    public Class<JWSToken> getTokenType() {
        return JWSToken.class;
    }

    private int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private boolean validateSignature(JWSToken token) {
        JWS jws = token.getJws();

        JWSBuilder builder = new JWSBuilder();
        List<String> roles = token.getRoles();
        List<String> groups = token.getGroups();
//        List<String> permissions = token.getPermissions();

        builder
                .id(jws.getId())
                .rsa256(privateKey)
                .issuer(jws.getIssuer())
                .issuedAt(jws.getIssuedAt())
                .subject(jws.getSubject())
                .expiration(jws.getExpiration())
                .notBefore(jws.getNotBefore())
                .claim("group", groups.toArray(new String[groups.size()]))
                .claim("role", roles.toArray(new String[roles.size()]));
//                .claim("permission", permissions.toArray(new String[permissions.size()]));

        JWSToken compareToken = new JWSToken(builder.build().encode());

        return token.getToken().equals(compareToken.getToken());
    }

    private boolean validateExpire(JWSToken token) {
        return getCurrentTime() < token.getJws().getExpiration();
    }

    private boolean validateNotBefore(JWSToken token) {
        return getCurrentTime() >= token.getJws().getNotBefore();
    }

    @Override
    public <I extends IdentityType> I extractIdentity(JWSToken token, Class<I> identityType, StereotypeProperty.Property stereotypeProperty, Object identifier) {
        if (token == null || token.getToken() == null) {
            throw MESSAGES.nullArgument("Token");
        }

        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (stereotypeProperty == null) {
            throw MESSAGES.nullArgument("Identifier value");
        }

        if (identifier == null) {
            throw MESSAGES.nullArgument("Identifier value");
        }

        return extractIdentityTypeFromToken(token, identityType, stereotypeProperty, identifier);
    }

    @Override
    public boolean hasPermission(JWSToken token, Permission permission) {
        String name;
        if (permission.getResourceClass() != null) {
            name = String.class.getName() + "[" + permission.getResourceIdentifier() + "]." + permission.getOperation();
        } else {
            name = permission.getResource() + "." + permission.getOperation();
        }

        return token.getPermissions().contains(name);
    }

    private <I extends IdentityType> I extractIdentityTypeFromToken(JWSToken token, Class<I> identityType, StereotypeProperty.Property stereotypeProperty, Object identifier) {
        if (hasIdentityType(token, stereotypeProperty, identifier)) {
            try {
                I identityTypeInstance = Reflections.newInstance(identityType);
                Property property = resolveProperty(identityType, stereotypeProperty);

                property.setValue(identityTypeInstance, identifier);

                if (Account.class.isAssignableFrom(identityType)) {
                    Property userNameProperty = resolveProperty(identityType, StereotypeProperty.Property.IDENTITY_ID);

                    userNameProperty.setValue(identityTypeInstance, extractSubject(token));
                }

                return identityTypeInstance;
            } catch (Exception e) {
                throw new IdentityManagementException("Could not extract IdentityType [" + identityType + "] from Token [" + token + "].", e);
            }
        }

        return null;
    }

    private Property resolveProperty(Class<? extends IdentityType> identityType, StereotypeProperty.Property stereotypeProperty) {
        List<Property<Object>> properties = PropertyQueries
                .createQuery(identityType)
                .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                .getResultList();

        if (properties.isEmpty()) {
            throw new IdentityManagementException("IdentityType [" + identityType + "] does not have any property mapped with " + StereotypeProperty.class + ".");
        }

        for (Property property : properties) {
            StereotypeProperty propertyStereotypeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);

            if (stereotypeProperty.equals(propertyStereotypeProperty.value())) {
                return property;
            }
        }

        throw new IdentityManagementException("Could not resolve property in type [" + identityType + " for StereotypeProperty [" + stereotypeProperty + ".");
    }

    private boolean hasIdentityType(JWSToken token, StereotypeProperty.Property stereotypeProperty, Object identifier) {
        if (StereotypeProperty.Property.IDENTITY_ROLE_NAME.equals(stereotypeProperty)) {
            Set<String> roleNames = extractRoles(token);

            if (roleNames.contains(identifier)) {
                return true;
            }
        }

        if (StereotypeProperty.Property.IDENTITY_GROUP_NAME.equals(stereotypeProperty)) {
            Set<String> groupNames = extractGroups(token);

            if (groupNames.contains(identifier)) {
                return true;
            }
        }

        if (StereotypeProperty.Property.IDENTITY_USER_NAME.equals(stereotypeProperty)
                || StereotypeProperty.Property.IDENTITY_ID.equals(stereotypeProperty)) {
            String subject = extractSubject(token);

            if (subject != null && identifier.equals(subject)) {
                return true;
            }
        }

        return false;
    }
}
