package com.inqool.dcap.office.api.core;

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
import java.time.LocalDateTime;
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

    public int createNewOaiSource(OaiSource oaiSource) {
        if(!hasUniqueNameOai(oaiSource)) {
            logger.error("Duplicate source name.");
            return -1;
        }
        if(!validateOai(oaiSource.getUrl())) {
            logger.error("Check oai source failed.");
            return -2;
        }
        em.persist(oaiSource);
        em.flush();
        return oaiSource.getId();
    }

    public int createNewSruSource(SruSource sruSource) {
        if(!hasUniqueNameSru(sruSource)) {
            logger.error("Duplicate source name.");
            return -1;
        }
        if(!validateSru(sruSource)) {
            logger.error("Check sru source failed.");
            return -2;
        }
        em.persist(sruSource);
        em.flush();
        return sruSource.getId();
    }

    public int createNewZ3950Source(Z3950Source z3950Source) {
        if(!hasUniqueNameZ3950(z3950Source)) {
            logger.error("Duplicate source name.");
            return -1;
        }
        if(!validateZ3950(z3950Source)) {
            logger.error("Check z3950 source failed.");
            return -2;
        }
        em.persist(z3950Source);
        em.flush();
        return z3950Source.getId();
    }

    public List<OaiSource> listOaiSources() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QOaiSource qOaiSource = QOaiSource.oaiSource;
        List<OaiSource> resultList = jpaQuery.from(qOaiSource).where(qOaiSource.deleted.eq(false)).list(qOaiSource);
        return resultList;
    }

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

    public OaiSource getOaiSource(int id) {
        OaiSource oaiSource = em.find(OaiSource.class, id);
        return oaiSource;
    }

    public SruSource getSruSource(int id) {
        SruSource sruSource = em.find(SruSource.class, id);
        return sruSource;
    }

    public Z3950Source getZ3950Source(int id) {
        Z3950Source z3950Source = em.find(Z3950Source.class, id);
        return z3950Source;
    }

    public void updateOaiSource(OaiSource updated) {
        if(!hasUniqueNameOai(updated)) {
            throw new RuntimeException("Duplicate source name.");
        }
        if(!validateOai(updated.getUrl())) {
            throw new RuntimeException("Check oai source failed.");
        }
        OaiSource oaiSource = em.find(OaiSource.class, updated.getId());
        if(oaiSource == null) {
            throw new RuntimeException("Can't find that source.");
        }
        oaiSource.setUrl(updated.getUrl());
        if("".equals(updated.getSet())) {
            oaiSource.setSet(null);
        }
        else {
            oaiSource.setSet(updated.getSet());
        }
        oaiSource.setName(updated.getName());
        oaiSource.setShortcut(updated.getShortcut());
    }

    public void updateSruSource(SruSource updated) {
        if(!hasUniqueNameSru(updated)) {
            throw new RuntimeException("Duplicate source name.");
        }
        if(!validateSru(updated)) {
            throw new RuntimeException("Check sru source failed.");
        }
        SruSource sruSource = em.find(SruSource.class, updated.getId());
        if(sruSource == null) {
            throw new RuntimeException("Can't find that source.");
        }
        sruSource.setUrl(updated.getUrl());
        sruSource.setName(updated.getName());
        sruSource.setDatabaseName(updated.getDatabaseName());
    }

    public void updateZ3950Source(Z3950Source updated) {
        if(!hasUniqueNameZ3950(updated)) {
            throw new RuntimeException("Duplicate source name.");
        }
        if(!validateZ3950(updated)) {
            throw new RuntimeException("Check z3950 source failed.");
        }
        Z3950Source z3950Source = em.find(Z3950Source.class, updated.getId());
        if(z3950Source == null) {
            throw new RuntimeException("Can't find that source.");
        }
        z3950Source.setUrl(updated.getUrl());
        z3950Source.setName(updated.getName());
        z3950Source.setDatabaseName(updated.getDatabaseName());
    }

    public void deleteOaiSource(int id) {
        OaiSource oaiSource = em.find(OaiSource.class, id);
        if(oaiSource == null) {
            throw new RuntimeException("Can't find that source.");
        }
        oaiSource.setDeleted(true);
    }

    public void deleteSruSource(int id) {
        SruSource sruSource = em.find(SruSource.class, id);
        if(sruSource == null) {
            throw new RuntimeException("Can't find that source.");
        }
        sruSource.setDeleted(true);
    }

    public void deleteZ3950Source(int id) {
        Z3950Source z3950Source = em.find(Z3950Source.class, id);
        if(z3950Source == null) {
            throw new RuntimeException("Can't find that source.");
        }
        z3950Source.setDeleted(true);
    }

    private boolean hasUniqueNameOai(OaiSource oaiSource) {
        JPAQuery query = new JPAQuery(em);
        QOaiSource qOaiSource = QOaiSource.oaiSource;
        OaiSource duplicate = query
                .from(qOaiSource)
                .where(qOaiSource.name.eq(oaiSource.getName())
                        .and(qOaiSource.id.ne(oaiSource.getId()))
                        .and(qOaiSource.deleted.eq(false)))
                .singleResult(qOaiSource);
        return duplicate == null;
    }

    private boolean hasUniqueNameSru(SruSource sruSource) {
        JPAQuery query = new JPAQuery(em);
        QSruSource qSruSource = QSruSource.sruSource;
        SruSource duplicate = query
                .from(qSruSource)
                .where(qSruSource.name.eq(sruSource.getName())
                        .and(qSruSource.id.ne(sruSource.getId()))
                        .and(qSruSource.deleted.eq(false)))
                .singleResult(qSruSource);
        return duplicate == null;
    }

    private boolean hasUniqueNameZ3950(Z3950Source z3950Source) {
        JPAQuery query = new JPAQuery(em);
        QZ3950Source qz3950Source = QZ3950Source.z3950Source;
        Z3950Source duplicate = query
                .from(qz3950Source)
                .where(qz3950Source.name.eq(z3950Source.getName())
                        .and(qz3950Source.id.ne(z3950Source.getId()))
                        .and(qz3950Source.deleted.eq(false)))
                .singleResult(qz3950Source);
        return duplicate == null;
    }

    private boolean validateOai(String url) {
        return oaiHarvester.isWorkingOaiSource(url);
    }

    private boolean validateSru(SruSource sruSource) {
        return sruClient.testSruSource(sruSource.getUrl() + sruSource.getDatabaseName());
    }

    private boolean validateZ3950(Z3950Source z3950Source) {
        //todo we may never know
        return true;
    }

    public void touchOaiSource(int id, LocalDateTime time) {
        OaiSource oaiSource = em.find(OaiSource.class, id);
        oaiSource.setLastHarvested(time);
    }
}
