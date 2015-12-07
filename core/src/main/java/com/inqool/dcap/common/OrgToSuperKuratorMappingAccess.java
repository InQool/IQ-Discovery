package com.inqool.dcap.common;

import com.inqool.dcap.common.entity.OrgToSuperKuratorMapping;
import com.inqool.dcap.common.entity.QOrgToSuperKuratorMapping;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.jpa.impl.JPADeleteClause;
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
public class OrgToSuperKuratorMappingAccess {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public void addMapping(String org, String superKurator) {
        //Delete previous superkurator of org
        QOrgToSuperKuratorMapping qOrgToSuperKuratorMapping = QOrgToSuperKuratorMapping.orgToSuperKuratorMapping;
        JPADeleteClause jpaDeleteClause = new JPADeleteClause(em, qOrgToSuperKuratorMapping);
        jpaDeleteClause.where(qOrgToSuperKuratorMapping.orgName.eq(org)).execute();

        //Add this one
        OrgToSuperKuratorMapping orgToSuperKuratorMapping = new OrgToSuperKuratorMapping();
        orgToSuperKuratorMapping.setOrgName(org);
        orgToSuperKuratorMapping.setSuperKuratorName(superKurator);
        em.persist(orgToSuperKuratorMapping);
    }

    public String getOrgSuperKurator(String org) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOrgToSuperKuratorMapping qOrgToSuperKuratorMapping = QOrgToSuperKuratorMapping.orgToSuperKuratorMapping;
        String superKurator = jpaQuery.from(qOrgToSuperKuratorMapping).where(qOrgToSuperKuratorMapping.orgName.eq(org)).singleResult(qOrgToSuperKuratorMapping.superKuratorName);
        return superKurator;
    }
}
