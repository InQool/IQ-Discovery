package com.inqool.dcap.common;

import com.inqool.dcap.common.dto.StatsWeeklyDto;
import com.inqool.dcap.common.entity.QStatsDocs;
import com.inqool.dcap.common.entity.QStatsWeekly;
import com.inqool.dcap.common.entity.StatsDocs;
import com.inqool.dcap.common.entity.StatsWeekly;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 28. 9. 2015.
 */
@ApplicationScoped
@Transactional
public class StatsAccessCommon {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public StatsDocs getStatDocs(String docInvId) {
        return em.find(StatsDocs.class, docInvId);
    }

    public void incrementDocViews(String docInvId) {
        StatsDocs statsDocs = em.find(StatsDocs.class, docInvId);
        if(statsDocs == null) {
            statsDocs = new StatsDocs();
            statsDocs.setFavorites(0);
            statsDocs.setViews(0);
            statsDocs.setDocInvId(docInvId);
            em.persist(statsDocs);
        }
        statsDocs.setViews(statsDocs.getViews() + 1);
    }

    public void incrementDocFavorites(String docInvId) {
        StatsDocs statsDocs = em.find(StatsDocs.class, docInvId);
        if(statsDocs == null) {
            statsDocs = new StatsDocs();
            statsDocs.setFavorites(0);
            statsDocs.setViews(0);
            statsDocs.setDocInvId(docInvId);
            em.persist(statsDocs);
        }
        statsDocs.setFavorites(statsDocs.getFavorites() + 1);
    }

    public void decrementDocFavorites(String docInvId) {
        StatsDocs statsDocs = em.find(StatsDocs.class, docInvId);
        if(statsDocs == null) {
            return;
        }
        statsDocs.setFavorites(statsDocs.getFavorites() - 1);
    }

    //Weekly stats

    private StatsWeekly fetchOrCreateStatsWeekly(String user, String organization) {
        LocalDateTime weekStart = findWeekStartDateTime();

        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsWeekly qStatsWeekly = QStatsWeekly.statsWeekly;
        StatsWeekly statsWeekly = jpaQuery.from(qStatsWeekly)
                .where(
                        qStatsWeekly.week.eq(weekStart)
                                .and(qStatsWeekly.userId.eq(user))
                                .and(qStatsWeekly.organization.eq(organization))
                )
                .singleResult(qStatsWeekly);
        if(statsWeekly == null) {
            statsWeekly = new StatsWeekly();
            statsWeekly.setUserId(user);
            statsWeekly.setOrganization(organization);
            statsWeekly.setWeek(weekStart);
        }
        return statsWeekly;
    }

    public void documentPublished(String user, String organization, int number) {
        StatsWeekly statsWeekly = fetchOrCreateStatsWeekly(user, organization);
        statsWeekly.setDocsPublished(statsWeekly.getDocsPublished() + number);
        if(!em.contains(statsWeekly)) {
            em.persist(statsWeekly);
        }
    }

    public void documentConcepted(String user, String organization, int number) {
        StatsWeekly statsWeekly = fetchOrCreateStatsWeekly(user, organization);
        statsWeekly.setDocsConcepted(statsWeekly.getDocsConcepted() + number);
        if(!em.contains(statsWeekly)) {
            em.persist(statsWeekly);
        }
    }

    public void documentReserved(String user, String organization) {
        documentReserved(user, organization, 1);
    }
    public void documentReserved(String user, String organization, int number) {
        StatsWeekly statsWeekly = fetchOrCreateStatsWeekly(user, organization);
        statsWeekly.setDocsReserved(statsWeekly.getDocsReserved() + number);
        if(!em.contains(statsWeekly)) {
            em.persist(statsWeekly);
        }
    }

    //Returns last monday 0:00
    private LocalDateTime findWeekStartDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .minusDays(dateTime.getDayOfWeek().getValue() - 1);
    }

    public List<StatsDocs> getTopViewedDocs(int limit) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsDocs qStatsDocs = QStatsDocs.statsDocs;
        List<StatsDocs> statsDocsList = jpaQuery
                .from(qStatsDocs)
                .orderBy(qStatsDocs.views.desc())
                .limit(limit)
                .list(qStatsDocs);
        return statsDocsList;
    }

    public List<StatsDocs> getTopFavoriteDocs(int limit) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsDocs qStatsDocs = QStatsDocs.statsDocs;
        List<StatsDocs> statsDocsList = jpaQuery
                .from(qStatsDocs)
                .where(qStatsDocs.favorites.goe(1))
                .orderBy(qStatsDocs.favorites.desc())
                .limit(limit)
                .list(qStatsDocs);
        return statsDocsList;
    }

    public List<StatsWeeklyDto> getWeeklyStats(int limit, String org, String user) {

        LocalDateTime thisWeekStart = findWeekStartDateTime();

        if(limit < 1) {
            limit = 1;
        }
        LocalDateTime firstWeekStart = thisWeekStart.minusWeeks(limit - 1);

        QStatsWeekly qStatsWeekly = QStatsWeekly.statsWeekly;
        JPAQuery jpaQuery = (new JPAQuery(em))
                .from(qStatsWeekly)
                .where(qStatsWeekly.week.between(firstWeekStart, thisWeekStart));

        List<Tuple> tupleList;
        if(user != null) {
            tupleList = jpaQuery
                    .where(qStatsWeekly.userId.eq(user))
                    .groupBy(qStatsWeekly.week)
                    .orderBy(qStatsWeekly.week.asc())
                    .list(qStatsWeekly.week, qStatsWeekly.docsConcepted.sum(), qStatsWeekly.docsPublished.sum(), qStatsWeekly.docsReserved.sum());
        }
        else if(org != null) {
            tupleList = jpaQuery
                    .where(qStatsWeekly.organization.eq(org))
                    .groupBy(qStatsWeekly.week)
                    .orderBy(qStatsWeekly.week.asc())
                    .list(qStatsWeekly.week, qStatsWeekly.docsConcepted.sum(), qStatsWeekly.docsPublished.sum(), qStatsWeekly.docsReserved.sum());
        }
        else  {
            tupleList = jpaQuery
                    .groupBy(qStatsWeekly.week)
                    .orderBy(qStatsWeekly.week.asc())
                    .list(qStatsWeekly.week, qStatsWeekly.docsConcepted.sum(), qStatsWeekly.docsPublished.sum(), qStatsWeekly.docsReserved.sum());
        }
        List<StatsWeeklyDto> resultList = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            resultList.add(
                    new StatsWeeklyDto(
                            tuple.get(qStatsWeekly.week),
                            tuple.get(qStatsWeekly.docsConcepted.sum()),
                            tuple.get(qStatsWeekly.docsPublished.sum()),
                            tuple.get(qStatsWeekly.docsReserved.sum())
                    )
            );
        }
        return resultList;
    }
}
