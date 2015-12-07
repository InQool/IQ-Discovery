package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 7. 7. 2015.
 */
@Entity
@Setter
@Getter
@Table
@JsonIgnoreProperties("fieldHandler")
public class OrgToSuperKuratorMapping {
    @Id
    @GeneratedValue
    @NotNull
    @Column
    private int id;

    private String orgName;
    private String superKuratorName;
}
