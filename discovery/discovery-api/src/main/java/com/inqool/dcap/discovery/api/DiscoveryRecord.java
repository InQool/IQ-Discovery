package com.inqool.dcap.discovery.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 23. 2. 2015.
 */
public class DiscoveryRecord {

    private String id;
    private Map<String, Collection<Object>> dcTerms = new HashMap<>();
    private String imageUrl;

    public Map<String, Collection<Object>> getDcTerms() {
        return dcTerms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
