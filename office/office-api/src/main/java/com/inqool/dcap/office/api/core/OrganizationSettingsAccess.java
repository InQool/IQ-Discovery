package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.OrganizationSettings;
import com.inqool.dcap.common.entity.QOrganizationSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.security.PicketLinkAccess;
import com.mysema.query.jpa.impl.JPAQuery;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.picketlink.Identity;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class OrganizationSettingsAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private Identity identity;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    @Inject
    private DataStore store;

    @Inject
    private ImgUploadTools imgUploadTools;

    //TODO DELETE ON PRODUCTION
    public int createOrgSettings(OrganizationSettings organizationSettings) {
        em.persist(organizationSettings);
        em.flush();
        return organizationSettings.getId();
    }
    //

    public OrganizationSettings fetchCurrentOrgSettings() {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        return fetchOrgSettings(orgIdmId);
    }

    public OrganizationSettings fetchOrgSettings(String orgIdmId) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOrganizationSettings qOrganizationSettings = QOrganizationSettings.organizationSettings;
        OrganizationSettings organizationSettings = jpaQuery
                .from(qOrganizationSettings)
                .where(qOrganizationSettings.orgIdmId.eq(orgIdmId)
                        .and(qOrganizationSettings.deleted.eq(false)))
                .singleResult(qOrganizationSettings);
        if (organizationSettings == null) {
            throw new RuntimeException("Can't get organization settings, not found.");
        }
        return organizationSettings;
    }

    public String fetchOrgWatermark(String orgIdmId) {
        return fetchOrgSettings(orgIdmId).getWatermarkId();
    }

    public void updateOrgSettings(String orgIdmId, OrganizationSettings organizationSettings) {
        OrganizationSettings organizationSettingsDb = fetchOrgSettings(orgIdmId);
        organizationSettingsDb.setCss(organizationSettings.getCss());
        organizationSettingsDb.setIpMaskPairs(organizationSettings.getIpMaskPairs());
    }

    public void updateCurrentOrgSettings(OrganizationSettings organizationSettings) {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        updateOrgSettings(orgIdmId, organizationSettings);
    }

    public String uploadHeader(MultipartFormDataInput input) throws IOException {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        return uploadHeaderOfOrg(input, orgIdmId);
    }

    public String uploadHeaderOfOrg(MultipartFormDataInput input, String orgIdmId) throws IOException {
        OrganizationSettings organizationSettings = fetchOrgSettings(orgIdmId);
        String currentHeaderId = organizationSettings.getHeaderId();
        if (currentHeaderId != null) {
            store.delete(store.createUrl(currentHeaderId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.orgHeader);

        organizationSettings.setHeaderId(uuid);
        return uuid;
    }

    public void deleteHeader() throws IOException {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        deleteHeaderOfOrg(orgIdmId);
    }

    public void deleteHeaderOfOrg(String orgIdmId) throws IOException {
        OrganizationSettings organizationSettings = fetchOrgSettings(orgIdmId);
        String currentHeaderId = organizationSettings.getHeaderId();
        if (currentHeaderId != null) {
            store.delete(store.createUrl(currentHeaderId));
        }
        organizationSettings.setHeaderId(null);
    }

    public String uploadLogo(MultipartFormDataInput input) throws IOException {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        return uploadLogoOfOrg(input, orgIdmId);
    }

    public String uploadLogoOfOrg(MultipartFormDataInput input, String orgIdmId) throws IOException {
        OrganizationSettings organizationSettings = fetchOrgSettings(orgIdmId);
        String currentLogoId = organizationSettings.getLogoId();
        if (currentLogoId != null) {
            store.delete(store.createUrl(currentLogoId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.orgLogo);

        organizationSettings.setLogoId(uuid);
        return uuid;
    }

    public void deleteLogo() throws IOException {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        deleteLogoOfOrg(orgIdmId);
    }

    public void deleteLogoOfOrg(String orgIdmId) throws IOException {
        OrganizationSettings organizationSettings = fetchOrgSettings(orgIdmId);
        String currentLogoId = organizationSettings.getLogoId();
        if (currentLogoId != null) {
            store.delete(store.createUrl(currentLogoId));
        }
        organizationSettings.setLogoId(null);
    }

    public String uploadWatermark(MultipartFormDataInput input) throws IOException {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        return uploadWatermarkOfOrg(input, orgIdmId);
    }

    public String uploadWatermarkOfOrg(MultipartFormDataInput input, String orgIdmId) throws IOException {
        OrganizationSettings organizationSettings = fetchOrgSettings(orgIdmId);
        String currentLogoId = organizationSettings.getLogoId();
        if (currentLogoId != null) {
            store.delete(store.createUrl(currentLogoId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.orgWatermark);

        organizationSettings.setWatermarkId(uuid);
        return uuid;
    }

    public void deleteWatermark() throws IOException {
        String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
        deleteWatermarkOfOrg(orgIdmId);
    }

    public void deleteWatermarkOfOrg(String orgIdmId) throws IOException {
        OrganizationSettings organizationSettings = fetchOrgSettings(orgIdmId);
        String currentWatermarkId = organizationSettings.getWatermarkId();
        if (currentWatermarkId != null) {
            store.delete(store.createUrl(currentWatermarkId));
        }
        organizationSettings.setWatermarkId(null);
    }
}
