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
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 4. 6. 2015.
 */
@Entity
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
public class Expo {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    @Size(max = 255)
    private String title;
    @Size(max = 255)
    private String perex;
    @Column(columnDefinition="TEXT")
    private String content;

    private String imageId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<ExpoDocument> documents = new ArrayList<>();

    /*private String solrQuery;*/
    @JsonIgnore
    private String owner;

    private boolean active;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime publishedFrom;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime publishedTo;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @JsonIgnore
    private boolean deleted = false;
}
