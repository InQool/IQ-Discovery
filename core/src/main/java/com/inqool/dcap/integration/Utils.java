package com.inqool.dcap.integration;

import gov.loc.marc21.slim.CollectionType;
import org.openarchives.oai._2_0.oai_dc.Dc;
import org.openarchives.oai._2_0.oai_dc.DcCollection;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 5. 1. 2015.
 */
public class Utils {
    public static CollectionType unmarshallMarcXml(String xml) {
        try {
            JAXBContext jc = JAXBContext.newInstance("gov.loc.marc21.slim");
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement root = (JAXBElement) u.unmarshal(new StringReader(xml));
            return (CollectionType) root.getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String marcXmlToDcCollectionXml(String xml) {
        TransformerFactory tFactory = TransformerFactory.newInstance();

        Source xmlDoc = new StreamSource(new StringReader(xml));
        Source xslDoc = new StreamSource(Utils.class.getClassLoader().getResourceAsStream("xslt/MARC21slim2OAIDC.xsl.xml"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Transformer trasform = tFactory.newTransformer(xslDoc);
            trasform.transform(xmlDoc, new StreamResult(baos));
            return baos.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String marcSrwXmlToDcCollectionSrwXml(String xml) {
        TransformerFactory tFactory=TransformerFactory.newInstance();

        Source xmlDoc = new StreamSource(new StringReader(xml));
        Source xslDoc = new StreamSource(Utils.class.getClassLoader().getResourceAsStream("xslt/SrwMARC21slim2SrwOAIDC.xsl.xml"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Transformer trasform = tFactory.newTransformer(xslDoc);
            trasform.transform(xmlDoc, new StreamResult(baos));
            return baos.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DcCollection unmarshallDcCollectionXml(String xml) {
        try {
            JAXBContext jc = JAXBContext.newInstance("org.openarchives.oai._2_0.oai_dc:org.purl.dc.elements._1");
            Unmarshaller u = jc.createUnmarshaller();
            return (DcCollection) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Map<String, List<String>>> dcCollectionToJsonSerializable(DcCollection dcCollection) {
        List<Map<String, List<String>>> result = new ArrayList<>();
        for(Dc dc : dcCollection.getDcs()) {
            Map<String, List<String>> recordMap = new HashMap<>();
            for(JAXBElement<SimpleLiteral> jaxbEl : dc.getTitlesAndCreatorsAndSubjects()) {
                recordMap.put(jaxbEl.getName().getLocalPart(), jaxbEl.getValue().getContent());
            }
            result.add(recordMap);
        }
        return result;
    }
}
