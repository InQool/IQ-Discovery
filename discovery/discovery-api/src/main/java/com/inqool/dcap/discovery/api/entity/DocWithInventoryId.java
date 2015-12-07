package com.inqool.dcap.discovery.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;

/**
 * @author Lukas Jane (inQool) 17. 8. 2015.
 */
@Getter
@Setter
@MappedSuperclass
public class DocWithInventoryId {
    private String docInvId;
}
