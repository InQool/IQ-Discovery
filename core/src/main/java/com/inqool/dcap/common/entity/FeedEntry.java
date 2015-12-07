package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class FeedEntry {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    @Size(max = 255)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    @Size(max = 255)
    private String link;

    private LocalDateTime created = LocalDateTime.now();

    @JsonIgnore
    private boolean deleted = false;
}
