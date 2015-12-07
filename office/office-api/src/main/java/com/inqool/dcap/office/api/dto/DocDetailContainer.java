package com.inqool.dcap.office.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 20. 5. 2015.
 */
@Getter
@Setter
public class DocDetailContainer {
    private Map<String, List<String>> concept;  //actually can be published doc
    private Map<String, List<String>> kdr;
    private Map<String, List<String>> bach;
    private Map<String, List<String>> demus;
    private Map<String, Map<String, List<String>>> oai = new HashMap<>();
}
