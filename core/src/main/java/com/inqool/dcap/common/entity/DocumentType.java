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
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Entity
@Setter
@Getter
@Table
@JsonIgnoreProperties("fieldHandler")
public class DocumentType {
    @Id
    @GeneratedValue
    @NotNull
    @Column
    private int id;

    private String name;

    @OneToMany(mappedBy = "owningType", fetch = FetchType.LAZY)
    private List<DocumentSubType> subTypes = new ArrayList<>();

    @JsonIgnore
    private boolean deleted = false;
}
