package com.inqool.dcap.office.api.entity;

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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 3. 3. 2015.
 */
@Entity
@Setter
@Getter
@Table
@JsonIgnoreProperties("fieldHandler")
public class ZdoBatch {
    @Id
    @GeneratedValue
    @NotNull
    @Column
    private int id;

    private String name;

    private String owner;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modified = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private BatchState state;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> documents = new ArrayList<>();

    @Transient
    private int numDocs = 0;

    @JsonIgnore
    private boolean deleted = false;

    public enum BatchState {
        published,
        unfinished,
        discarded
    }

    //It IS used by json serialization
    public int getNumDocs() {
        return documents.size();
    }
}
