package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.StatsAccessCommon;
import com.inqool.dcap.config.Zdo;
import org.slf4j.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Lukas Jane (inQool) 28. 9. 2015.
 */
@Stateless
@Dependent
public class StatsAsyncLayer {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private StatsAccessCommon statsAccessCommon;

    @Asynchronous
    public void incrementDocViews(String docInvId) {
        statsAccessCommon.incrementDocViews(docInvId);
    }

    @Asynchronous
    public void incrementDocFavorites(String docInvId) {
        statsAccessCommon.incrementDocFavorites(docInvId);
    }

    @Asynchronous
    public void decrementDocFavorites(String docInvId) {
        statsAccessCommon.decrementDocFavorites(docInvId);
    }
}
