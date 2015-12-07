package com.inqool.dcap.integration.z3950.server;

import org.jzkit.z3950.server.Z3950Listener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Lukas Jane (inQool) 16. 1. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
public class ServerRunner {

//    @Inject
//    @Zdo
//    private Logger logger;

    private Z3950Listener listener;

    @PostConstruct
    public void startZ3950Listener() {

//        logger.debug("Starting jzkit2 z3950 server...");

        ApplicationContext app_context = new ClassPathXmlApplicationContext("z3950ServerContext.xml");
        listener = (Z3950Listener)app_context.getBean("Z3950Listener",Z3950Listener.class);
        listener.start();

//        logger.debug("Listener up and running");
    }

    @PreDestroy
    public void stopZ3950Listener() {
        if(listener != null) {
            listener.shutdown(0);
        }
    }

    public static void main(String[] args) {
        new ServerRunner().startZ3950Listener();
    }

}
