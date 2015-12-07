/*
 * Web.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.inqool.dcap.ip;

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
