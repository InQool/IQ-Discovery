package com.inqool.dcap.office.api.dto;

import com.inqool.dcap.integration.model.ZdoType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Lukas Jane (inQool) 18. 3. 2015.
 */
@Getter
@Setter
public class ZdoDocumentForExpo {
    private String invId;
    private String title;
    private ZdoType type;
    private String orgName;
}
