package com.inqool.dcap.common.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * @author Lukas Jane (inQool) 4. 6. 2015.
 */
@Embeddable
@Getter
@Setter
public class OrganizationSettingsIpPair {
    private String ip;
    private String mask;
}
