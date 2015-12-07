package com.inqool.dcap.office.config.filter;

import com.googlecode.webutilities.filters.common.AbstractFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.googlecode.webutilities.common.Constants.DEFAULT_EXPIRES_MINUTES;
import static com.googlecode.webutilities.common.Constants.HEADER_EXPIRES;
import static com.googlecode.webutilities.util.Utils.readLong;


public class ExpiresFilter extends AbstractFilter {
    public static final String INIT_PARAM_EXPIRES_MINUTES = "expiresMinutes";

    private long expiresMinutes = DEFAULT_EXPIRES_MINUTES; //default value 7 days

    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        this.expiresMinutes = readLong(config.getInitParameter(INIT_PARAM_EXPIRES_MINUTES), this.expiresMinutes);
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        chain.doFilter(req, resp);

        if (resp.containsHeader(HEADER_EXPIRES)) {
            resp.setDateHeader(HEADER_EXPIRES, new Date().getTime() + expiresMinutes * 60 * 1000);
        } else {
            resp.addDateHeader(HEADER_EXPIRES, new Date().getTime() + expiresMinutes * 60 * 1000);
        }
    }
}
