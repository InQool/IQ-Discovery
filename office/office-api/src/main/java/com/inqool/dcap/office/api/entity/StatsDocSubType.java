package com.inqool.dcap.office.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inqool.dcap.common.entity.DocumentSubType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 4. 6. 2015.
 */
@Entity
@Getter
@Setter
@JsonIgnoreProperties("fieldHandler")
public class StatsDocSubType {
    @Id
    @GeneratedValue
    @NotNull
    @Column
    private int id;

    @OneToOne
    @JoinColumn
    private DocumentSubType docSubType;

    private String organization;

    private int numPublished = 0;
}
