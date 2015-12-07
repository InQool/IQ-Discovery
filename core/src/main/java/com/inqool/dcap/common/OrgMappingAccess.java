package com.inqool.dcap.common;

import com.inqool.dcap.common.entity.OrgIcoToNumberMapping;
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
public class OrgMappingAccess {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public void addOrg(String ico, String orgId) {
        OrgIcoToNumberMapping orgIcoToNumberMapping = new OrgIcoToNumberMapping();
        orgIcoToNumberMapping.setIco(ico);
        orgIcoToNumberMapping.setOrgId(orgId);
        em.persist(orgIcoToNumberMapping);
    }

    public String getOrgId(String ico) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOrgIcoToNumberMapping qOrgIcoToNumberMapping = QOrgIcoToNumberMapping.orgIcoToNumberMapping;
        String orgId = jpaQuery.from(qOrgIcoToNumberMapping).where(qOrgIcoToNumberMapping.ico.eq(ico)).singleResult(qOrgIcoToNumberMapping.orgId);
        return orgId;
    }

    public String getOrgIco(String id) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOrgIcoToNumberMapping qOrgIcoToNumberMapping = QOrgIcoToNumberMapping.orgIcoToNumberMapping;
        String orgIco = jpaQuery.from(qOrgIcoToNumberMapping).where(qOrgIcoToNumberMapping.orgId.eq(id)).singleResult(qOrgIcoToNumberMapping.ico);
        return orgIco;
    }
}
