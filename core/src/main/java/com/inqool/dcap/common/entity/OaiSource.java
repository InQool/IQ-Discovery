package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Entity
@Setter
@Getter
@Table
@JsonIgnoreProperties("fieldHandler")
public class OaiSource {
    @Id
    @GeneratedValue
    @NotNull
    @Column
    private int id = 0;

    private String name;

    private String shortcut;

    private String url;

    private String set;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastHarvested;

    @JsonIgnore
    private boolean deleted = false;
}
