package com.inqool.dcap.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lukas Jane (inQool) 4. 6. 2015.
 */
@Getter
@Setter
public class StatsDocsDto {
    private String docInvId;
    private String title;
    private long views;
    private int favorites;
}
