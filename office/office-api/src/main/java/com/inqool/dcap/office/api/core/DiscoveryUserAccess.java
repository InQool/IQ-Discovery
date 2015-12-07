package com.inqool.dcap.office.api.core;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.entity.DiscoveryUserFake;
import com.inqool.dcap.office.api.entity.QDiscoveryUserFake;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class DiscoveryUserAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private MailNotifier mailNotifier;

    public DiscoveryUserFake getUserByOpNumber(String opNumber) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QDiscoveryUserFake qDiscoveryUser = QDiscoveryUserFake.discoveryUserFake;
        DiscoveryUserFake discoveryUser = jpaQuery
                .from(qDiscoveryUser)
                .where(qDiscoveryUser.opNumber.eq(opNumber))
                .singleResult(qDiscoveryUser);
        return discoveryUser;
    }

    public boolean markUserAsVerified(DiscoveryUserFake postedUser) throws IOException {
        DiscoveryUserFake oldUser = getUserByOpNumber(postedUser.getOpNumber());
        if(oldUser == null) {
            logger.info("No matching discovery user found.");
            return false;
        }
        if(oldUser.isVerified()) {
            logger.info("User has already already been verified.");
            return false;
        }
        oldUser.setFirstName(postedUser.getFirstName());
        oldUser.setLastName(postedUser.getLastName());
        oldUser.setCity(postedUser.getCity());
        oldUser.setZip(postedUser.getZip());
        oldUser.setStreet(postedUser.getStreet());
        oldUser.setStreetNumber(postedUser.getStreetNumber());
        oldUser.setOpNumber(postedUser.getOpNumber());
        oldUser.setVerified(true);
        oldUser.setVerifiedDate(LocalDateTime.now());
        /*oldUser.setVerification_token(null);*/

        mailNotifier.notifyUserVerified(postedUser.getFirstName(), postedUser.getLastName(), oldUser.getLoginName());

        //auto-persist
        return true;
    }
}
