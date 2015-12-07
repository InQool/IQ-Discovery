package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.OrganizationSettings;
import com.inqool.dcap.common.entity.QOrganizationSettings;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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
    private DataStore store;

    public OrganizationSettings fetchOrgSettings(String orgIdmId) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOrganizationSettings qOrganizationSettings = QOrganizationSettings.organizationSettings;
        OrganizationSettings organizationSettings = jpaQuery
                .from(qOrganizationSettings)
                .where(qOrganizationSettings.orgIdmId.eq(orgIdmId)
                        .and(qOrganizationSettings.deleted.eq(false)))
                .singleResult(qOrganizationSettings);
        if(organizationSettings == null) {
            throw new RuntimeException("Can't get organization settings, not found.");
        }
        return organizationSettings;
    }

    public String fetchOrgWatermarkId(String orgIdmId) {
        return fetchOrgSettings(orgIdmId).getWatermarkId();
    }

    public String fetchOrgCss(String orgIdmId) {
        return fetchOrgSettings(orgIdmId).getCss();
    }
}
