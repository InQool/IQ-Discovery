package com.inqool.dcap.office.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inqool.dcap.config.LocalDateTimeSerializer;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.config.LocalDateTimeDeserializer;
import com.inqool.dcap.office.api.resource.Document;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 18. 3. 2015.
 */
@Getter
@Setter
public class ZdoDocumentBrief {
    private String id;
    private String invId;
    private String title;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modified;
    private Document.DocumentState state;
    private Document.DocumentState publishingState;
    private ZdoType type;
    private String batchName;
    private int batchId;
    private boolean validToPublish;
}
