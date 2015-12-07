package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.*;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.SruClient;
import com.inqool.dcap.integration.oai.harvester.OaiHarvester;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@RequestScoped
@Transactional
public class ExternalSourcesAccess {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OaiHarvester oaiHarvester;

    @Inject
    private SruClient sruClient;

    public List<SruSource> listSruSources() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QSruSource qSruSource = QSruSource.sruSource;
        List<SruSource> resultList = jpaQuery.from(qSruSource).where(qSruSource.deleted.eq(false)).list(qSruSource);
        return resultList;
    }

    public List<Z3950Source> listZ3950Sources() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QZ3950Source qz3950Source = QZ3950Source.z3950Source;
        List<Z3950Source> resultList = jpaQuery.from(qz3950Source).where(qz3950Source.deleted.eq(false)).list(qz3950Source);
        return resultList;
    }

    public List<OaiSource> listOaiSources() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOaiSource qOaiSource = QOaiSource.oaiSource;
        List<OaiSource> resultList = jpaQuery.from(qOaiSource).where(qOaiSource.deleted.eq(false)).list(qOaiSource);
        return resultList;
    }

    public SruSource getSruSource(int id) {
        SruSource sruSource = em.find(SruSource.class, id);
        return sruSource;
    }

    public Z3950Source getZ3950Source(int id) {
        Z3950Source z3950Source = em.find(Z3950Source.class, id);
        return z3950Source;
    }

    public OaiSource getOaiSource(int id) {
        OaiSource oaiSource = em.find(OaiSource.class, id);
        return oaiSource;
    }
}
