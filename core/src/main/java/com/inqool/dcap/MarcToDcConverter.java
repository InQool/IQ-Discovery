package com.inqool.dcap;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.model.ZdoType;
import gov.loc.marc21.slim.ControlFieldType;
import gov.loc.marc21.slim.DataFieldType;
import gov.loc.marc21.slim.RecordType;
import gov.loc.marc21.slim.SubfieldatafieldType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 2. 9. 2015.
 */
public class MarcToDcConverter {

    private Map<String, Map<String, Property>> mapping = new HashMap<>();

    public MarcToDcConverter() {
        addMapping("015", "a", DCTerms.identifier);
        addMapping("041", "a", DCTerms.language);
        addMapping("100", "a", DCTerms.creator);
        addMapping("110", "a", DCTerms.creator);
        addMapping("111", "a", DCTerms.creator);
        addMapping("700", "a", DCTerms.creator); /*DCTerms.contributor*/
        addMapping("710", "a", DCTerms.creator);
        addMapping("711", "a", DCTerms.creator);
        addMapping("245", "ab", DCTerms.title);
        addMapping("246", "a", DCTerms.title);
        addMapping("260", "ab", DCTerms.publisher);
        addMapping("260", "c", DCTerms.created);
        addMapping("300", "abc", DCTerms.format);
        addMapping("520", "a", DCTerms.description);
        addMapping("521", "a", DCTerms.description);
        addMapping("650", "a", DCTerms.subject);
        addMapping("655", "a", DCTerms.type);
        addMapping("856", "q", DCTerms.format);
    }

    public void addMapping(String field, String subfield, Property prop) {
        if(!mapping.containsKey(field)) {
            mapping.put(field, new HashMap<>());
        }
        for(int i = 0; i < subfield.length(); i++)
        {
            mapping.get(field).put(String.valueOf(subfield.charAt(i)), prop);
        }
    }

    public void convert(RecordType record, String orgPrefix, ZdoModel model) {
        for(ControlFieldType controlField : record.getControlfields()) {
            if(controlField.getTag().equals("001")) {
                model.replaceValueOfProperty(ZdoTerms.inventoryId, orgPrefix.toUpperCase() + "_" + controlField.getValue().toUpperCase());
            }
            if(controlField.getTag().equals("008")) {
                if(model.get(ZdoTerms.zdoType) == null) {
                    String typeMark = controlField.getValue().substring(6, 7);
                    switch (typeMark) {
                        case "c":
                        case "d":
                        case "u":
                            model.add(ZdoTerms.zdoType, ZdoType.periodical.name());
                            break;
                        default:
                            model.add(ZdoTerms.zdoType, ZdoType.monograph.name());
                    }
                }
            }
        }
        for(DataFieldType dataField : record.getDatafields()) {
            Map<String, Property> innerMapping = mapping.get(dataField.getTag());
            if(innerMapping != null) {
                for(SubfieldatafieldType subfield : dataField.getSubfields()) {
                    Property prop = innerMapping.get(subfield.getCode());
                    if(prop != null) {
                        String value = subfield.getValue();
                        //ISBD records can have title ending by special char like "/", we don't want that in db
                        if(prop.equals(DCTerms.title) && (value.endsWith("/") || value.endsWith(":") || value.endsWith(";"))) {
                            value = value.substring(0, value.length() - 1).trim();
                        }
                        model.add(prop, value);
                    }
                }
            }
        }
    }
}
