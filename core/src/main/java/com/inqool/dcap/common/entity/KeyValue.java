package com.inqool.dcap.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 27. 11. 2015.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties("fieldHandler")
@Entity
public class KeyValue {
    @Id
    @NotNull
    String key;
    String value;

    @Transient
    public static final String kdrDocsToLoadRemaining = "kdrDocsToLoadRemaining";
}
