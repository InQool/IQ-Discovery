package com.inqool.dcap.discovery.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
* @author Lukas Jane (inQool) 17. 8. 2015.
*/
@Getter
@Setter
public class DocumentReservationDto {  //reused also for clipboard documents
    private int id;
    private String fedoraId;
    private String invId;
    private String title;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    private String reason;
}
