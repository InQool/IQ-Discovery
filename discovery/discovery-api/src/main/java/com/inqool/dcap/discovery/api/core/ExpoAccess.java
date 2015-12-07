package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.Expo;
import com.inqool.dcap.common.entity.QExpo;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.resource.Search;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class ExpoAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private Search search;

    public Expo fetchPublishedExpo(int expoId) {
        Expo expo = em.find(Expo.class, expoId);
        if(expo == null || expo.isDeleted()) {
            throw new RuntimeException("Can't get expo, expo not found.");
        }
        if(!expo.isActive()
                || (expo.getPublishedFrom() != null && expo.getPublishedFrom().isAfter(LocalDateTime.now()))
                || (expo.getPublishedTo() != null && expo.getPublishedTo().isBefore(LocalDateTime.now()))
                ) {
            throw new RuntimeException("This expo isn't published.");
        }
        return expo;
    }

    public List<Expo> listPublishedExpos() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QExpo qExpo = QExpo.expo;
        List<Expo> expoList = jpaQuery
                .from(qExpo)
                .where(qExpo.deleted.eq(false)
                                .and(qExpo.active.eq(true))
                                .and(qExpo.publishedFrom.isNull().or(qExpo.publishedFrom.before(LocalDateTime.now())))
                                .and(qExpo.publishedTo.isNull().or(qExpo.publishedTo.after(LocalDateTime.now())))
                                )
                        .orderBy(qExpo.created.desc())
                .list(qExpo);
        return expoList;
    }

    public List<Expo> listPublishedExpoHeaders() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QExpo qExpo = QExpo.expo;
        List<Expo> expoList = jpaQuery
                .from(qExpo)
                .where(qExpo.deleted.eq(false)
                                .and(qExpo.active.eq(true))
                                .and(qExpo.publishedFrom.isNull().or(qExpo.publishedFrom.before(LocalDateTime.now())))
                                .and(qExpo.publishedTo.isNull().or(qExpo.publishedTo.after(LocalDateTime.now())))
                                )
                        .orderBy(qExpo.created.desc())
                .list(qExpo);
        return expoList;
    }
}