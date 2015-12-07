package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 4. 6. 2015.
 */
@Entity
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
public class Article {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    @Size(max = 255)
    private String title;
    @Size(max = 255)
    private String perex;
    @Size(max = 255)
    private String url; //not used anymore
    @Column(columnDefinition="TEXT")
    private String content;

    private String imageId;

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

    private String owner;
    private String authorName;
    private String authorOrgName;

    @JsonIgnore
    private boolean deleted = false;
}
