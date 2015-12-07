package com.inqool.dcap.office.api;

import com.inqool.dcap.common.entity.OaiSource;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.oai.harvester.OaiHarvester;
import com.inqool.dcap.office.api.core.ExternalSourcesAccess;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Regularly runs OAI harvest.
 * @author Lukas Jane (inQool) 8. 6. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class HarvestingScheduler {

    @Inject
    @Zdo
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private OaiHarvester oaiHarvester;

    @Inject
    private ExternalSourcesAccess externalSourcesAccess;

    @Inject
    private ProjectStage projectStage;

    @Inject
    @ConfigProperty(name = "oai.schedule.minutes")
    private String minutes;
    @Inject
    @ConfigProperty(name = "oai.schedule.hours")
    private String hours;
    @Inject
    @ConfigProperty(name = "oai.schedule.dayOfWeek")
    private String daysOfWeek;
    @Inject
    @ConfigProperty(name = "oai.schedule.dayOfMonth")
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
    public void finish(Timer timer) {
        /*timer.getInfo();*/
        try {
            List<OaiSource> oaiSources = externalSourcesAccess.listOaiSources();
            for(OaiSource oaiSource : oaiSources) {
                try {
                    LocalDateTime harvestTime = LocalDateTime.now();
                    oaiHarvester.harvestSource(oaiSource);
                    externalSourcesAccess.touchOaiSource(oaiSource.getId(), harvestTime);
                } catch (Exception e) {
                    logger.error("Scheduled OAI harvest failed.", e);
                }
            }
        } catch (Exception e) {
            logger.error("Scheduled OAI harvest failed.", e);
        }
    }
}
