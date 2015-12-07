package com.inqool.dcap.integration.z3950.client;

import gov.loc.marc21.slim.CollectionType;
import org.openarchives.oai._2_0.oai_dc.DcCollection;

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
        TransformerFactory tFactory=TransformerFactory.newInstance();

        Source xmlDoc = new StreamSource(new StringReader(xml));
        Source xslDoc = new StreamSource(Utils.class.getClassLoader().getResourceAsStream("schemas/MARC21slim2OAIDC.xsl.xml"));

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
            JAXBContext jc = JAXBContext.newInstance("org.openarchives.oai._2_0.oai_dc");
            Unmarshaller u = jc.createUnmarshaller();
            return (DcCollection) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
