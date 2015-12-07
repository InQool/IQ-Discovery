package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.MailNotification;
import com.inqool.dcap.common.entity.QMailNotification;
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
public class MailNotificationAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    public List<MailNotification> listMailNotifications() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QMailNotification qMailNotification = QMailNotification.mailNotification;
        List<MailNotification> mailNotificationList = jpaQuery.from(qMailNotification).where(qMailNotification.deleted.eq(false)).list(qMailNotification);
        return mailNotificationList;
    }

    public int createMailNotification(MailNotification mailNotification) {
        em.persist(mailNotification);
        em.flush();
        return mailNotification.getId();
    }

    public MailNotification fetchMailNotification(int mailNotificationId) {
        MailNotification mailNotification = em.find(MailNotification.class, mailNotificationId);
        if(mailNotification == null || mailNotification.isDeleted()) {
            throw new RuntimeException("Can't get mail notification, not found.");
        }
        return mailNotification;
    }

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

    public void updateMailNotification(int mailNotificationId, MailNotification updatedMailNotification) {
        MailNotification dbMailNotification = em.find(MailNotification.class, mailNotificationId);
        dbMailNotification.setSubject(updatedMailNotification.getSubject());
        dbMailNotification.setText(updatedMailNotification.getText());
    }

    public void deleteMailNotification(int mailNotificationId) {
        MailNotification mailNotification = em.find(MailNotification.class, mailNotificationId);
        mailNotification.setDeleted(true);
    }

    public void deleteMailNotifications(List<Integer> mailNotificationIds) {
        mailNotificationIds.forEach(this::deleteMailNotification);
    }
}
