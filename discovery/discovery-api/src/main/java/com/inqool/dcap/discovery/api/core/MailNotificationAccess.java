package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.MailNotification;
import com.inqool.dcap.common.entity.QMailNotification;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class MailNotificationAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public MailNotification fetchMailNotification(String name) {
        QMailNotification qMailNotification = QMailNotification.mailNotification;
        JPAQuery jpaQuery = new JPAQuery(em);
        MailNotification mailNotification = jpaQuery.from(qMailNotification)
                .where(qMailNotification.name.eq(name)
                .and(qMailNotification.deleted.eq(false)))
                .singleResult(qMailNotification);
        if(mailNotification == null) {
            throw new RuntimeException("Mail notification not found in database: " + name);
        }
        return mailNotification;
    }
}
