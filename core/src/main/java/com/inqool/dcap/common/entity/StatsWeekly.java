package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 4. 6. 2015.
 */
@Entity
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
public class StatsWeekly {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime week;

    private String organization;
    private String userId;

    private int docsConcepted = 0;
    private int docsPublished = 0;
    private int docsReserved = 0;
}
