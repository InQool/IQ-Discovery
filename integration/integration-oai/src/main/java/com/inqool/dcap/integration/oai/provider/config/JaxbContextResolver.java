package com.inqool.dcap.integration.oai.provider.config;

import org.openarchives.oai._2.OAIPMH;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Needed for marshalling Dublin Core and other format responses inside OAI-PMH envelopes
 * @author Lukas Jane (inQool)
 */
@Provider
@Produces ({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

    @Override
    public JAXBContext getContext(Class<?> type) {
        if (type == OAIPMH.class) {
            String contextPath = "org.openarchives.oai._2" +
                    ":org.openarchives.oai._2_0.oai_dc" +
                    ":org.purl.dc.elements._1" +
                    ":org.purl.dc.terms" +
                    ":eu.europeana.schemas.ese";
            try {
                return JAXBContext.newInstance(contextPath);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
