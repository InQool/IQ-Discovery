package com.inqool.dcap.integration.z3950.sru.config;

import info.srw.schema._1.dc_schema.DcCollectionType;
import org.oasis_open.docs.ns.search_ws.sruresponse.ExplainResponseDefinition;
import org.oasis_open.docs.ns.search_ws.sruresponse.SearchRetrieveResponseDefinition;
import org.purl.dc.elements._1.ObjectFactory;
import org.z3950.explain.dtd._2.Explain;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Needed for marshalling some classes inside other classes, they are missing in JAXB context so they must be added here.
 * @author Lukas Jane (inQool)
 */
@Provider
@Produces ({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

    @Override
    public JAXBContext getContext(Class<?> type) {
        try {
            if (type == DcCollectionType.class) {
                return JAXBContext.newInstance("" +
                                "info.srw.schema._1.dc_schema:" +
                                "org.purl.dc.elements._1"
                );
            }
            else if (type == ExplainResponseDefinition.class) {
                return JAXBContext.newInstance(Explain.class, ExplainResponseDefinition.class);
            }
            else if(type == SearchRetrieveResponseDefinition.class) {
                return JAXBContext.newInstance(DcCollectionType.class, ObjectFactory.class, SearchRetrieveResponseDefinition.class); //contains even SrwDcType
            }
            else {
                return null;
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
