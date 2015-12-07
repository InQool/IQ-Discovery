package com.inqool.dcap.office.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 5. 7. 2015.
 */
@Getter
@Setter
public class StatsUserDto {
    @Id
    @NotNull
    private String userId;

    private String firstName;
    private String lastName;
    private int docsPublished = 0;
}
