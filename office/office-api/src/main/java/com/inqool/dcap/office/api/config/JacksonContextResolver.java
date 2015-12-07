package com.inqool.dcap.office.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Both Jackson classes are needed to properly serialize OffsetDateTime to JSON
* @author Lukas Jane (inQool) 10. 3. 2015.
*/
@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
@Produces({MediaType.APPLICATION_JSON, "text/json"})
class JacksonContextResolver implements ContextResolver<ObjectMapper> {
    @Inject
    private ObjectMapper objectMapper;

    public JacksonContextResolver() {

    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
