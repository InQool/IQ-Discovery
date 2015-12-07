package com.inqool.dcap.common;

import com.inqool.dcap.common.entity.FeedEntry;
import com.inqool.dcap.common.entity.QFeedEntry;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class FeedAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public void addToFeed(FeedEntry feedEntry) {
        em.persist(feedEntry);
    }

    public List<FeedEntry> listFeed() {
        QFeedEntry qFeedEntry = QFeedEntry.feedEntry;
        JPAQuery jpaQuery = new JPAQuery(em);
        List<FeedEntry> feedEntries = jpaQuery.from(qFeedEntry)
                .where(qFeedEntry.deleted.eq(false))
                .limit(50)
                .orderBy(qFeedEntry.created.desc())
                .list(qFeedEntry);
        return feedEntries;
    }
}
