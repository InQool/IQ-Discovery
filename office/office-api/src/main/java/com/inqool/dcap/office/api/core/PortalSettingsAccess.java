package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.PortalSettings;
import com.inqool.dcap.common.entity.QPortalSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.service.DataStore;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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
public class PortalSettingsAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private DataStore store;

    @Inject
    private ImgUploadTools imgUploadTools;

    public PortalSettings fetchPortalSettings() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QPortalSettings qPortalSettings = QPortalSettings.portalSettings;
        PortalSettings portalSettings = jpaQuery
                .from(qPortalSettings)
                .singleResult(qPortalSettings);
        return portalSettings;
    }

    public void updatePortalSettings(PortalSettings portalSettings) {
        QPortalSettings qPortalSettings = QPortalSettings.portalSettings;
        JPAUpdateClause jpaUpdateClause = new JPAUpdateClause(em, qPortalSettings);
        jpaUpdateClause
                .set(qPortalSettings.discoveryTitle, portalSettings.getDiscoveryTitle())
                .set(qPortalSettings.discoverySubTitle, portalSettings.getDiscoverySubTitle())
                .set(qPortalSettings.css, portalSettings.getCss())
                .execute();
    }

    public String uploadPortalHeader(MultipartFormDataInput input) throws IOException {
        PortalSettings portalSettings = fetchPortalSettings();
        String currentHeaderId = portalSettings.getHeaderId();
        if (currentHeaderId != null) {
            store.delete(store.createUrl(currentHeaderId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.portalHeader);

        portalSettings.setHeaderId(uuid);
        return uuid;
    }

    public void deletePortalHeader() throws IOException {
        PortalSettings portalSettings = fetchPortalSettings();
        String currentHeaderId = portalSettings.getHeaderId();
        if (currentHeaderId != null) {
            store.delete(store.createUrl(currentHeaderId));
        }
        portalSettings.setHeaderId(null);
    }

    public String uploadPortalLogo(MultipartFormDataInput input) throws IOException {
        PortalSettings portalSettings = fetchPortalSettings();
        String currentLogoId = portalSettings.getLogoId();
        if (currentLogoId != null) {
            store.delete(store.createUrl(currentLogoId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.portalLogo);

        portalSettings.setLogoId(uuid);
        return uuid;
    }

    public void deletePortalLogo() throws IOException {
        PortalSettings portalSettings = fetchPortalSettings();
        String currentLogoId = portalSettings.getLogoId();
        if (currentLogoId != null) {
            store.delete(store.createUrl(currentLogoId));
        }
        portalSettings.setLogoId(null);
    }

    public String uploadPortalWatermark(MultipartFormDataInput input) throws IOException {
        PortalSettings portalSettings = fetchPortalSettings();
        String currentLogoId = portalSettings.getLogoId();
        if (currentLogoId != null) {
            store.delete(store.createUrl(currentLogoId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.portalWatermark);

        portalSettings.setWatermarkId(uuid);
        return uuid;
    }

    public void deletePortalWatermark() throws IOException {
        PortalSettings portalSettings = fetchPortalSettings();
        String currentWatermarkId = portalSettings.getWatermarkId();
        if (currentWatermarkId != null) {
            store.delete(store.createUrl(currentWatermarkId));
        }
        portalSettings.setWatermarkId(null);
    }
}
