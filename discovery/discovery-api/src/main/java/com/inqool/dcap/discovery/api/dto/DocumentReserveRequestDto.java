package com.inqool.dcap.discovery.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @author Lukas Jane (inQool) 17. 8. 2015.
*/
@Getter
@Setter
public class DocumentReserveRequestDto {  //reused also for clipboard documents
    private List<String> fedoraIds;
    private String reason;
}
