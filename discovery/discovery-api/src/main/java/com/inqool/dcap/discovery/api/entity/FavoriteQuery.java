package com.inqool.dcap.discovery.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 17. 8. 2015.
 */
@Getter
@Setter
@Entity
@JsonIgnoreProperties("fieldHandler")
public class FavoriteQuery {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    @JsonIgnore
    private String userId;

    private String solrQuery;
    private String query;
    private String restrictions;
}
