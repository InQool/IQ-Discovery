package com.inqool.dcap.discovery.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 17. 8. 2015.
 */
@Getter
@Setter
@Entity
public class ClipboardDocument extends DocWithInventoryId {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    private String userId;
}
