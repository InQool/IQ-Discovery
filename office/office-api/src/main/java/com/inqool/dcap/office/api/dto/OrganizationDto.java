package com.inqool.dcap.office.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lukas Jane (inQool) 20. 5. 2015.
 */
@Getter
@Setter
public class OrganizationDto {
    private String id;
    private String name;
    private int userCount;
}
