package com.inqool.dcap.discovery.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
* @author Lukas Jane (inQool) 17. 8. 2015.
*/
@Getter
@Setter
public class FavoriteDocumentDto {  //reused also for clipboard documents
    private int id;
    private String fedoraId;
    private String invId;
    private String title;
}
