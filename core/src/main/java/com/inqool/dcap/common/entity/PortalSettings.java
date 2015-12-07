package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 16. 7. 2015.
 */
@Entity
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
public class PortalSettings {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    private String discoveryTitle;
    private String discoverySubTitle;
    private String css;

    private String headerId;
    private String logoId;
    private String watermarkId;
}
