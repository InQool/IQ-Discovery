package com.inqool.dcap.office.config.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.googlecode.webutilities.common.Constants;
import com.googlecode.webutilities.filters.common.AbstractFilter;
import com.googlecode.webutilities.servlets.JSCSSMergeServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.googlecode.webutilities.common.Constants.DEFAULT_CACHE_CONTROL;
import static com.googlecode.webutilities.common.Constants.DEFAULT_EXPIRES_MINUTES;
import static com.googlecode.webutilities.util.Utils.*;


public class ResponseCacheFilter extends AbstractFilter {

    public static final String CACHE_HEADER = "X-ResponseCacheFilter";

    public static enum CacheState {FOUND, NOT_FOUND, ADDED, SKIPPED}

    private class CacheObject {

        private long time;

        //private long accessCount = 0;

        private WebUtilitiesResponseWrapper webUtilitiesResponseWrapper;

        CacheObject(long time, WebUtilitiesResponseWrapper webUtilitiesResponseWrapper) {
            this.time = time;
            this.webUtilitiesResponseWrapper = webUtilitiesResponseWrapper;
        }

        public long getTime() {
            return time;
        }

        public WebUtilitiesResponseWrapper getWebUtilitiesResponseWrapper() {
            return webUtilitiesResponseWrapper;
        }

        /*public void increaseAccessCount(){
            accessCount++;
        }

        public long getAccessCount(){
            return this.accessCount;
        }*/

    }

    private static Cache<String, CacheObject> buildCache(/*int reloadAfterAccess, */int reloadAfterWrite) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().softValues();
        // if(reloadAfterAccess > 0)
        //     builder.expireAfterAccess(reloadAfterAccess, TimeUnit.SECONDS);
        if (reloadAfterWrite > 0)
            builder.expireAfterWrite(reloadAfterWrite, TimeUnit.SECONDS);
        return builder.build();
    }

    private Cache<String, CacheObject> cache;

    private int resetTime = 0;

    private long lastResetTime;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCacheFilter.class.getName());

    private static final String INIT_PARAM_RELOAD_TIME = "reloadTime";

    private static final String INIT_PARAM_RESET_TIME = "resetTime";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        int reloadTime = readInt(filterConfig.getInitParameter(INIT_PARAM_RELOAD_TIME), 0);

        this.resetTime = readInt(filterConfig.getInitParameter(INIT_PARAM_RESET_TIME), resetTime);

        lastResetTime = new Date().getTime();

        if (cache == null) // fixme: checking for letting the unit test happy but nothing.
            cache = buildCache(reloadTime);

        LOGGER.debug("Cache Filter initialized with: {}:{},\n{}:{}",
                INIT_PARAM_RELOAD_TIME, String.valueOf(reloadTime),
                INIT_PARAM_RESET_TIME, String.valueOf(resetTime));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String url = httpServletRequest.getRequestURI();

        httpServletResponse.setHeader(CACHE_HEADER, CacheState.SKIPPED.toString());

        if (!isURLAccepted(url)
                || !isQueryStringAccepted(httpServletRequest.getQueryString())
                || !isUserAgentAccepted(httpServletRequest.getHeader(Constants.HTTP_USER_AGENT_HEADER))) {
            LOGGER.debug("Skipping Cache filter for: {}?{}", url, httpServletRequest.getQueryString());
            LOGGER.debug("URL, QueryString or UserAgent not accepted");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }


        long now = new Date().getTime();

        CacheObject cacheObject = cache.getIfPresent(url);

        boolean expireCache = httpServletRequest.getParameter(Constants.PARAM_EXPIRE_CACHE) != null;

        if (expireCache) {
            LOGGER.trace("Removing Cache for {}  due to URL parameter.", url);
            cache.invalidate(url);
        }

        boolean resetCache = httpServletRequest.getParameter(Constants.PARAM_RESET_CACHE) != null ||
                resetTime > 0 && (now - lastResetTime) / 1000 > resetTime;

        if (resetCache) {
            LOGGER.trace("Resetting whole Cache for {} due to URL parameter.", url);
            cache.invalidateAll(); // fixme: we don't need reset since cache values are soft referenced.
            lastResetTime = now;
        }

        boolean skipCache = httpServletRequest.getParameter(Constants.PARAM_DEBUG) != null || httpServletRequest.getParameter(Constants.PARAM_SKIP_CACHE) != null;

        if (skipCache) {
            filterChain.doFilter(servletRequest, servletResponse);
            LOGGER.trace("Skipping Cache for {} due to URL parameter.", url);
            return;
        }

        List<String> requestedResources = findResourcesToMerge(httpServletRequest.getContextPath(), url);
        ServletContext context = filterConfig.getServletContext();
        String extensionOrPath = detectExtension(url);//in case of non js/css files it null
        if (extensionOrPath == null) {
            extensionOrPath = requestedResources.get(0);//non grouped i.e. non css/js file, we refer it's path in that case
        }

        JSCSSMergeServlet.ResourceStatus status = JSCSSMergeServlet.isNotModified(context, httpServletRequest, requestedResources, false);
        if (status.isNotModified()) {
            LOGGER.trace("Resources Not Modified. Sending 304.");
            cache.invalidate(url);
            JSCSSMergeServlet.sendNotModified(httpServletResponse, extensionOrPath, status.getActualETag(), DEFAULT_EXPIRES_MINUTES, DEFAULT_CACHE_CONTROL);
            return;
        }

        boolean cacheFound = false;

        if (cacheObject != null && cacheObject.getWebUtilitiesResponseWrapper() != null) {
            if (requestedResources != null && isAnyResourceModifiedSince(requestedResources, cacheObject.getTime(), context)) {
                LOGGER.trace("Some resources have been modified since last cache: {}", url);
                cache.invalidate(url);
                cacheFound = false;
            } else {
                LOGGER.trace("Found valid cached response.");
                //cacheObject.increaseAccessCount();
                cacheFound = true;
            }
        }

        if (cacheFound) {
            LOGGER.debug("Returning Cached response.");
            cacheObject.getWebUtilitiesResponseWrapper().fill(httpServletResponse);
            httpServletResponse.setHeader(CACHE_HEADER, CacheState.FOUND.toString());
            //fillResponseFromCache(httpServletResponse, cacheObject.getModuleResponse());
        } else {
            LOGGER.trace("Cache not found or invalidated");
            httpServletResponse.setHeader(CACHE_HEADER, CacheState.NOT_FOUND.toString());
            WebUtilitiesResponseWrapper wrapper = new WebUtilitiesResponseWrapper(httpServletResponse);
            filterChain.doFilter(servletRequest, wrapper);

            // some filters return no status code, but we believe that it is "200 OK"
            if (wrapper.getStatus() == 0) {
                wrapper.setStatus(200);
            }

            if (isMIMEAccepted(wrapper.getContentType()) && !expireCache && !resetCache && wrapper.getStatus() == 200) { //Cache only 200 status response
                cache.put(url, new CacheObject(getLastModifiedFor(requestedResources, context), wrapper));
                LOGGER.debug("Cache added for: {}", url);
                httpServletResponse.setHeader(CACHE_HEADER, CacheState.ADDED.toString());
            } else {
                LOGGER.trace("Cache NOT added for: {}", url);
                LOGGER.trace("is MIME not accepted: {}", isMIMEAccepted(wrapper.getContentType()));
                LOGGER.trace("is expireCache: {}", expireCache);
                LOGGER.trace("is resetCache: {}", resetCache);
            }
            wrapper.fill(httpServletResponse);
        }

    }
}





