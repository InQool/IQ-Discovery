package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.FeedAccess;
import com.inqool.dcap.common.entity.FeedEntry;
import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * Regularly generates rss and atom feed files.
 * @author Lukas Jane (inQool) 10. 9. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class FeedGeneratorScheduler {

    @Inject
    @Zdo
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private FeedAccess feedAccess;

    @Inject
    private FeedCreator feedCreator;

    @Inject
    @ConfigProperty(name = "feed.rebuild.schedule.minutes")
    private String minutes;
    @Inject
    @ConfigProperty(name = "feed.rebuild.schedule.hours")
    private String hours;
    @Inject
    @ConfigProperty(name = "feed.rebuild.schedule.dayOfWeek")
    private String daysOfWeek;
    @Inject
    @ConfigProperty(name = "feed.rebuild.schedule.dayOfMonth")
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
            List<FeedEntry> feedEntries = feedAccess.listFeed();
            feedCreator.generateFeeds(feedEntries);
        } catch (Exception e) {
            logger.error("Scheduled feed generation failed.", e);
        }
    }
}
