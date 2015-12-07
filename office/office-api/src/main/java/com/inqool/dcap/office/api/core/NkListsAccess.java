package com.inqool.dcap.office.api.core;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.entity.nklists.*;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */

@Transactional
@RequestScoped
public class NkListsAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    private static final int MAX_RESULTS = 50;

    //Geographical authorities search
    public List<String> searchProxGeo(String name) {
        JPAQuery query = new JPAQuery(em);
        QGeographicalAuthority qGeographicalAuthority = QGeographicalAuthority.geographicalAuthority;
        List<String> resultList = query.from(qGeographicalAuthority)
                .where(qGeographicalAuthority.name.like("%" + name + "%"))
                .limit(MAX_RESULTS)
                .list(qGeographicalAuthority.name);
        return resultList;
    }
    public boolean existsGeo(String name) {
        GeographicalAuthority geographicalAuthority = em.find(GeographicalAuthority.class, name);
        return geographicalAuthority != null;
    }
    public void makeSureGeoExists(String name) {
        if(!existsGeo(name)) {
            GeographicalAuthority geographicalAuthority = new GeographicalAuthority(name, null);
            em.persist(geographicalAuthority);
        }
    }

    //Chronological authorities search
    public List<String> searchProxChro(String name) {
        JPAQuery query = new JPAQuery(em);
        QChronologicalAuthority qChronologicalAuthority = QChronologicalAuthority.chronologicalAuthority;
        List<String> resultList = query.from(qChronologicalAuthority)
                .where(qChronologicalAuthority.name.like("%" + name + "%"))
                .limit(MAX_RESULTS)
                .list(qChronologicalAuthority.name);
        return resultList;
    }
    public boolean existsChro(String name) {
        ChronologicalAuthority chronologicalAuthority = em.find(ChronologicalAuthority.class, name);
        return chronologicalAuthority != null;
    }
    public void makeSureChroExists(String name) {
        if(!existsChro(name)) {
            ChronologicalAuthority chronologicalAuthority = new ChronologicalAuthority(name, null);
            em.persist(chronologicalAuthority);
        }
    }

    //Topic authorities search
    public List<String> searchProxTopic(String name) {
        JPAQuery query = new JPAQuery(em);
        QTopicAuthority qTopicAuthority = QTopicAuthority.topicAuthority;
        List<String> resultList = query.from(qTopicAuthority)
                .where(qTopicAuthority.name.like("%" + name + "%"))
                .limit(MAX_RESULTS)
                .list(qTopicAuthority.name);
        return resultList;
    }
    public boolean existsTopic(String name) {
        TopicAuthority topicAuthority = em.find(TopicAuthority.class, name);
        return topicAuthority != null;
    }
    public void makeSureTopicExists(String name) {
        if(!existsTopic(name)) {
            TopicAuthority topicAuthority = new TopicAuthority(name, null);
            em.persist(topicAuthority);
        }
    }

    //Genre authorities search
    public List<String> searchProxGenre(String name) {
        JPAQuery query = new JPAQuery(em);
        QGenreAuthority qGenreAuthority = QGenreAuthority.genreAuthority;
        List<String> resultList = query.from(qGenreAuthority)
                .where(qGenreAuthority.name.like("%" + name + "%"))
                .limit(MAX_RESULTS)
                .list(qGenreAuthority.name);
        return resultList;
    }
    public boolean existsGenre(String name) {
        GenreAuthority genreAuthority = em.find(GenreAuthority.class, name);
        return genreAuthority != null;
    }
    public void makeSureGenreExists(String name) {
        if(!existsGenre(name)) {
            GenreAuthority genreAuthority = new GenreAuthority(name, null);
            em.persist(genreAuthority);
        }
    }

    //Save list of authorities to db
    public void saveAuthorityList(List<Object> list) {
        logger.info("Persisting authority list to database.");
        for (Object authority : list) {
            if(authority instanceof GenreAuthority) {
                GenreAuthority existingAuthority = em.find(GenreAuthority.class, ((GenreAuthority) authority).getName());
                if(existingAuthority != null) {
                    logger.debug("Some authority already existed and will be replaced: " + existingAuthority.getName());
                    em.merge(authority);
                }
                else {
                    em.persist(authority);
                }
            }
            if(authority instanceof GeographicalAuthority) {
                GeographicalAuthority existingAuthority = em.find(GeographicalAuthority.class, ((GeographicalAuthority) authority).getName());
                if(existingAuthority != null) {
                    logger.debug("Some authority already existed and will be replaced: " + existingAuthority.getName());
                    em.merge(authority);
                }
                else {
                    em.persist(authority);
                }
            }
            if(authority instanceof TopicAuthority) {
                TopicAuthority existingAuthority = em.find(TopicAuthority.class, ((TopicAuthority) authority).getName());
                if(existingAuthority != null) {
                    logger.debug("Some authority already existed and will be replaced: " + existingAuthority.getName());
                    em.merge(authority);
                }
                else {
                    em.persist(authority);
                }
            }
            if(authority instanceof ChronologicalAuthority) {
                ChronologicalAuthority existingAuthority = em.find(ChronologicalAuthority.class, ((ChronologicalAuthority) authority).getName());
                if(existingAuthority != null) {
                    logger.debug("Some authority already existed and will be replaced: " + existingAuthority.getName());
                    em.merge(authority);
                }
                else {
                    em.persist(authority);
                }
            }
        }
    }

    //Finds if database is empty and needs to be reloaded
    public boolean listsNotLoaded() {
        JPAQuery query = new JPAQuery(em);
        QGenreAuthority qGenreAuthority = QGenreAuthority.genreAuthority;
        String result = query.from(qGenreAuthority)
                .where(qGenreAuthority.code.isNotNull())
                .limit(1)
                .singleResult(qGenreAuthority.name);
        return result == null;
    }

    //Delete all authorities lists from db
    public void wipeAuthorities() {
        Query query = em.createNativeQuery("DELETE FROM geographical_authority WHERE code IS NULL; DELETE FROM chronological_authority WHERE code IS NULL; DELETE FROM topic_authority WHERE code IS NULL; DELETE FROM genre_authority WHERE code IS NULL;");
        query.executeUpdate();
    }
}
