package com.inqool.dcap.integration.desa2.loader;

import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Regularly runs synchronization of users and organizations with IDM.
 * @author Lukas Jane (inQool) 8. 6. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class DataLoadScheduler {

    @Inject
    @Zdo
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private KdrLoader kdrLoader;

    @Inject
    private BachXlsLoader bachXlsLoader;

    @Inject
    private DemusMdbLoader demusMdbLoader;

    @Inject
    @ConfigProperty(name = "kdr.schedule.minutes")
    private String minutes;
    @Inject
    @ConfigProperty(name = "kdr.schedule.hours")
    private String hours;
    @Inject
    @ConfigProperty(name = "kdr.schedule.dayOfWeek")
    private String daysOfWeek;
    @Inject
    @ConfigProperty(name = "kdr.schedule.dayOfMonth")
    private String daysOfMonth;

    @PostConstruct
    public void schedule() {
        ScheduleExpression scheduleExpression = new ScheduleExpression();
        scheduleExpression.minute(minutes);
        scheduleExpression.hour(hours);
        scheduleExpression.dayOfWeek(daysOfWeek);
        scheduleExpression.dayOfMonth(daysOfMonth);
        timerService.createCalendarTimer(scheduleExpression, new TimerConfig(null, false));
    }

    @Timeout
    private void finish(Timer timer) {
        /*timer.getInfo();*/
        try {
            kdrLoader.loadKdrData();
            bachXlsLoader.loadAvailableBachData();
            demusMdbLoader.loadAvailableDemusData();
        } catch (Exception e) {
            logger.error("Scheduled KDR synchronization failed.", e);
        }
    }
}
