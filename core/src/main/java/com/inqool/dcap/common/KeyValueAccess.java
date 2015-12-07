package com.inqool.dcap.common;

import com.inqool.dcap.common.entity.KeyValue;
import com.inqool.dcap.common.entity.QStatsWeekly;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
* @author Lukas Jane (inQool) 27. 11. 2015.
*/
@Transactional
@ApplicationScoped
public class KeyValueAccess {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public void store(String key, String value) {
        KeyValue keyValue = new KeyValue();
        keyValue.setKey(key);
        keyValue.setValue(value);
        em.merge(keyValue);
    }

    public String get(String key) {
        KeyValue keyValue = em.find(KeyValue.class, key);
        if(keyValue == null) {
            return null;
        }
        return keyValue.getValue();
    }

    //Wtf, without this never-called-function it fails to deploy integration-ip, with it, it deploys.
    //I must really be missing something here, but what? No time to find.
    private void bullshit() {
        QStatsWeekly qStatsWeekly = QStatsWeekly.statsWeekly;
        JPAQuery jpaQuery = (new JPAQuery(em))
                .from(qStatsWeekly);
    }
}
