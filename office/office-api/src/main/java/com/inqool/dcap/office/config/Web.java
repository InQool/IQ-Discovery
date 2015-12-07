package com.inqool.dcap.office.config;

import com.googlecode.webutilities.filters.CharacterEncodingFilter;
import com.googlecode.webutilities.filters.CompressionFilter;
import com.inqool.dcap.office.config.filter.ExpiresFilter;
import com.inqool.dcap.office.config.filter.ResponseCacheFilter;
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

        FilterRegistration.Dynamic expire = ctx.addFilter("expiresFilter", ExpiresFilter.class);
        expire.setInitParameter(ExpiresFilter.INIT_PARAM_EXPIRES_MINUTES, "43200");
        expire.addMappingForUrlPatterns(null, true, "*.js", "*.css");

        FilterRegistration.Dynamic caching = ctx.addFilter("responseCacheFilter", ResponseCacheFilter.class);
        caching.addMappingForUrlPatterns(null, true, "*.js", "*.css");

        FilterRegistration.Dynamic encoding = ctx.addFilter("characterEncodingFilter", CharacterEncodingFilter.class);
        encoding.setInitParameter("encoding", "UTF-8");
        encoding.setInitParameter("force", "true");
        encoding.addMappingForUrlPatterns(null, true, "*.js", "*.css");

        FilterRegistration.Dynamic compression = ctx.addFilter("compressionFilter", CompressionFilter.class);
        compression.setInitParameter("compressionThreshold", "512");
        compression.setInitParameter("ignoreURLPattern", ".*\\.(png|gif|jpg|tiff|bmp|mpg|jpeg)");
        compression.addMappingForUrlPatterns(null, true, "/*");



        FilterRegistration.Dynamic restEasy = ctx.addFilter("RestEasy", FilterDispatcher.class);
        restEasy.addMappingForUrlPatterns(null, true, "/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
