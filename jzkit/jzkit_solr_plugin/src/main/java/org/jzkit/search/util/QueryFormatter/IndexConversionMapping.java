package org.jzkit.search.util.QueryFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
public class IndexConversionMapping {
    private Map<String, Map<String, String>> mapping;

    public IndexConversionMapping() {
        this.mapping = new HashMap<String, Map<String, String>>();

        Map<String, String> dublinCores = new HashMap<String, String>();
        dublinCores.put("title", "title");
        dublinCores.put("creator", "creator");
        dublinCores.put("author", "creator");
        this.mapping.put("dc", dublinCores);

        Map<String, String> bib1s = new HashMap<String, String>();
        bib1s.put("4", "title");
        //bib1s.put("1003", "creator");
        bib1s.put("1003", "creator");
        this.mapping.put("bib-1:1", bib1s);

        Map<String, String> bib1s2 = new HashMap<String, String>();
        bib1s2.put("1.4", "title");
        //bib1s.put("1003", "creator");
        bib1s2.put("1.1003", "creator");
        this.mapping.put("bib-1", bib1s2);
    }

    public Map<String, Map<String, String>> getMapping() {
        return mapping;
    }
}
