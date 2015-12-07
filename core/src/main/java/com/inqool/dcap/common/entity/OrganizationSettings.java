package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 16. 7. 2015.
 */
@Entity
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
public class OrganizationSettings {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    private String orgIdmId;

    private String css = "";

    private String headerId;
    private String logoId;
    private String watermarkId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<OrganizationSettingsIpPair> ipMaskPairs = new ArrayList<>();

    @JsonIgnore
    private boolean deleted = false;
}
