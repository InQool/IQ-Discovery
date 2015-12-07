package com.inqool.dcap.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Lukas Jane (inQool) 15. 1. 2015.
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(Integer.valueOf(jsonParser.getText())), ZoneOffset.systemDefault());
        /*return LocalDateTime.parse(jsonParser.getText().trim(), DateTimeFormatter.ISO_ZONED_DATE_TIME);*/
    }
}
