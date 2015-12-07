package com.inqool.dcap.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 25. 9. 2015.
 */
public class AdditionalMetadata {
    private Map<String, List<String>> metadataMap = new HashMap<>();

    public AdditionalMetadata() {
    }

    public AdditionalMetadata(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TypeReference<HashMap<String, List<String>>> typeRef = new TypeReference<HashMap<String, List<String>>>() {};
            metadataMap = objectMapper.readValue(data, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed when deserializing additional metadata map.", e);
        }
    }

    public static AdditionalMetadata ofModel(ZdoModel model) {
        AdditionalMetadata additionalMetadata;
        String aditionalMetadataStr = model.get(ZdoTerms.additionalMetadata);
        if(aditionalMetadataStr != null) {
            additionalMetadata = new AdditionalMetadata(aditionalMetadataStr);
        }
        else {
            additionalMetadata = new AdditionalMetadata();
        }
        return additionalMetadata;
    }

    public void fitToModel(ZdoModel model) {
        if(!metadataMap.isEmpty()) {
            model.replaceValueOfProperty(ZdoTerms.additionalMetadata, this.toString());
        }
    }

    public String toString() {
        try {
            return (new ObjectMapper()).writeValueAsString(metadataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed when serializing additional metadata map.", e);
        }
/*        String result = "{";
        for(Map.Entry<String, String> entry : metadataMap.entrySet()) {
            result += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",";
        }
        if(result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        result += "}";
        return result;*/
    }

    public Map<String, List<String>> getMetadataMap() {
        return metadataMap;
    }

    public void addIfNotEmpty(String prop, String val) {
        if(val != null && !val.isEmpty()) {
            List<String> innerList = metadataMap.get(prop);
            if(innerList == null) {
                innerList = new ArrayList<>();
                metadataMap.put(prop, innerList);
            }
            innerList.add(val);
        }
    }
}
