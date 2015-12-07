package com.inqool.dcap.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Lukas Jane (inQool) 9. 9. 2015.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PdfCreatorDto {
    private String pdfId;
    private List<String> imageIds;
    private List<String> altoIds;
    private String watermarkId;
    private String watermarkPosition;
}
