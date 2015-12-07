package com.inqool.dcap.office.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Both Jackson classes are needed to properly serialize OffsetDateTime to JSON
* @author Lukas Jane (inQool) 10. 3. 2015.
*/
@ApplicationScoped
public class JacksonObjectMapperProducer {

    @Produces
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
}
