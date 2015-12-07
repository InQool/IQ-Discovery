package com.inqool.dcap.config;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author Lukas Jane (inQool) 15. 1. 2015.
 */
@Converter(autoApply = true)
public class OffsetDateTimeConverter implements AttributeConverter<OffsetDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime entityValue) {
        return Timestamp.valueOf(entityValue.toLocalDateTime());
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp databaseValue) {
        if (databaseValue == null) {
            return OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        }
        return OffsetDateTime.of(databaseValue.toLocalDateTime(), ZoneOffset.UTC);
    }
}

