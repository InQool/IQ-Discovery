package com.inqool.dcap.discovery.config;

import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.Arrays;
import java.util.HashSet;

/**
* Initializes application without web.xml file. Set cookie tracking and add JAX-RS filter to default servlet.
*/
@WebListener
class Web implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();

        ctx.setSessionTrackingModes(new HashSet<>(Arrays.asList(SessionTrackingMode.COOKIE)));

        FilterRegistration.Dynamic restEasy = ctx.addFilter("RestEasy", FilterDispatcher.class);
        restEasy.addMappingForUrlPatterns(null, true, "/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
