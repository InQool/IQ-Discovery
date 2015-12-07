package com.inqool.dcap.integration.desa2.loader;

import com.inqool.dcap.common.entity.QOrgIcoToNumberMapping;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
* @author Lukas Jane (inQool) 7. 7. 2015.
*/

@Transactional
@RequestScoped
public class OrgMappingAccess2 {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public String getOrgId(String ico) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOrgIcoToNumberMapping qOrgIcoToNumberMapping = QOrgIcoToNumberMapping.orgIcoToNumberMapping;
        String orgId = jpaQuery.from(qOrgIcoToNumberMapping).where(qOrgIcoToNumberMapping.ico.eq(ico)).singleResult(qOrgIcoToNumberMapping.orgId);
        if(orgId == null) {
            logger.error("Organization with ico " + ico + " has no pairing with organization id.");
        }
        return orgId;
    }
}
