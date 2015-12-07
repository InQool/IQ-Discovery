package com.inqool.dcap.discovery.security.handler;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AccessDeniedExceptionHandler implements ExceptionMapper<AccessDeniedException> {
    @Inject
    private HttpServletRequest request;

    @Override
    public Response toResponse(AccessDeniedException exception) {
        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
