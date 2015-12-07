package com.inqool.dcap.integration.z3950.sru.server.config;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
@ApplicationScoped
public class IndexConversionMapping {
    private Map<String, Map<String, String>> mapping;

    public IndexConversionMapping() {
        this.mapping = new HashMap<>();

        Map<String, String> dublinCores = new HashMap<>();
        dublinCores.put("title", "title");
        dublinCores.put("creator", "creator");
        this.mapping.put("dc", dublinCores);

        Map<String, String> bib1s = new HashMap<>();
        bib1s.put("4", "title");
        bib1s.put("1003", "creator");
        this.mapping.put("bib-1:1", bib1s);
    }

    public Map<String, Map<String, String>> getMapping() {
        return mapping;
    }
}
