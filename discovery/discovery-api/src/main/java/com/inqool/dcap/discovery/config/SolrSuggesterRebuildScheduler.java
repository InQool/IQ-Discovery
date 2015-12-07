package com.inqool.dcap.discovery.config;

import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * Regularly runs OAI harvest.
 * @author Lukas Jane (inQool) 8. 6. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class SolrSuggesterRebuildScheduler {

    @Inject
    @Zdo
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    @ConfigProperty(name = "solr.endpoint.suggester")
    private String SOLR_SUGGESTER_ENDPOINT;

    @Inject
    @ConfigProperty(name = "suggester.rebuild.schedule.minutes")
    private String minutes;
    @Inject
    @ConfigProperty(name = "suggester.rebuild.schedule.hours")
    private String hours;
    @Inject
    @ConfigProperty(name = "suggester.rebuild.schedule.dayOfWeek")
    private String daysOfWeek;
    @Inject
    @ConfigProperty(name = "suggester.rebuild.schedule.dayOfMonth")
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
            Response response = ClientBuilder.newClient()
                    .target(SOLR_SUGGESTER_ENDPOINT + "suggest?suggest.build=true")
                    .request()
                    .get();
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logger.error("Solr suggester rebuild call failed. " + response.getStatusInfo().getReasonPhrase());
            }
            else {
                logger.info("Solr suggester rebuild succesful.");
            }
        } catch (Exception e) {
            logger.error("Solr suggester rebuild call failed.", e);
        }
    }
}
