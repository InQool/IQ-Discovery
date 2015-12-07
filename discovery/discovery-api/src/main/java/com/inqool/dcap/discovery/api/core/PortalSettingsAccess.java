package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.PortalSettings;
import com.inqool.dcap.common.entity.QPortalSettings;
import com.inqool.dcap.config.Zdo;
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
public class PortalSettingsAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public PortalSettings fetchPortalSettings() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QPortalSettings qPortalSettings = QPortalSettings.portalSettings;
        PortalSettings portalSettings = jpaQuery
                .from(qPortalSettings)
                .singleResult(qPortalSettings);
        return portalSettings;
    }
}
