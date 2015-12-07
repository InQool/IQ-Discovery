package com.inqool.dcap.discovery.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 21. 7. 2015.
 */
@Getter
@Setter
public class Facet {
    private String name;
    private Map<String, Long> hitMap = new HashMap<>();
}
