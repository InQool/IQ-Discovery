package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.exception.PmhException;
import org.openarchives.oai._2.OAIPMH;
import org.openarchives.oai._2.OAIPMHerrorType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
public class BadVerbResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle() throws PmhException {
        OAIPMH rootType = createRootType();

        OAIPMHerrorType error = new OAIPMHerrorType();
        error.setCode(OAIPMHerrorcodeType.BAD_VERB);
        if(request.getParameter("verb") == null) {
            error.setValue("A verb parameter must be specified!");
        }
        else {
            error.setValue("Verb '" + request.getParameter("verb") + "' is not supported by OAI PMH protocol!");
        }
        rootType.getErrors().add(error);

        return rootType;
    }
}
