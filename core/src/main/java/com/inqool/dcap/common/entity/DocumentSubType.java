package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Entity
@Setter
@Getter
@Table
@JsonIgnoreProperties("fieldHandler")
public class DocumentSubType {
    @Id
    @GeneratedValue
    @NotNull
    @Column
    private int id;

    private String name;

    @JoinColumn
    @ManyToOne
    @JsonIgnore
    private DocumentType owningType;

    @JsonIgnore
    private boolean deleted = false;
}
