package com.inqool.dcap.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 9. 9. 2015.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsWeeklyDto {
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime week;
    private Integer docsConcepted;
    private Integer docsPublished;
    private Integer docsReserved;
}
