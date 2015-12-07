//package com.inqool.dcap.office.api;
//
//import com.inqool.dcap.config.CustomProjectStageHolder;
//import com.inqool.dcap.config.Zdo;
//import com.inqool.dcap.security.PicketLinkAccess;
//import org.apache.deltaspike.core.api.config.ConfigProperty;
//import org.apache.deltaspike.core.api.projectstage.ProjectStage;
//import org.slf4j.Logger;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import javax.ejb.*;
//import javax.enterprise.context.ApplicationScoped;
//import javax.inject.Inject;
//
///**
// * Regularly runs OAI harvest.
// * @author Lukas Jane (inQool) 8. 6. 2015.
// */
//@ApplicationScoped
//@Singleton
//@Startup
//@TransactionAttribute(TransactionAttributeType.NEVER)
//public class AutoPublishScheduler {
//
//    @Inject
//    @Zdo
//    private Logger logger;
//
//    @Resource
//    private TimerService timerService;
//
//    @Inject
//    private ProjectStage projectStage;
//
//    @Inject
//    private AutoPublisher autoPublisher;
//
//    @Inject
//    private PicketLinkAccess plAccess;
//
//    @Inject
//    @ConfigProperty(name = "autopublish.schedule.minutes")
//    private String minutes;
//    @Inject
//    @ConfigProperty(name = "autopublish.schedule.hours")
//    private String hours;
//    @Inject
//    @ConfigProperty(name = "autopublish.schedule.dayOfWeek")
//    private String daysOfWeek;
//    @Inject
//    @ConfigProperty(name = "autopublish.schedule.dayOfMonth")
//    private String daysOfMonth;
//
//    @PostConstruct
//    public void schedule() {
//        if(CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) {
//            ScheduleExpression scheduleExpression = new ScheduleExpression();
//            scheduleExpression.minute(minutes);
//            scheduleExpression.hour(hours);
//            scheduleExpression.dayOfWeek(daysOfWeek);
//            scheduleExpression.dayOfMonth(daysOfMonth);
//            timerService.createCalendarTimer(scheduleExpression, new TimerConfig(null, false));
//        }
//    }
//
//    @Timeout
//    public void finish(Timer timer) {
//        /*timer.getInfo();*/
//        if(CustomProjectStageHolder.DevelopmentLukess.equals(projectStage) || CustomProjectStageHolder.StagingSCK.equals(projectStage) || CustomProjectStageHolder.ProductionSCK.equals(projectStage)) {
//            try {
//                autoPublisher.autopublish();
//            } catch (Exception e) {
//                logger.error("Scheduled auto-publish failed.", e);
//            }
//        }
//    }
//}
