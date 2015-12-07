package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.exception.BadResumptionTokenPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoSetHierarchyPmhException;
import com.inqool.dcap.integration.oai.provider.request.ListSetsRequest;
import org.jboss.resteasy.annotations.Form;
import org.openarchives.oai._2.ListSetsType;
import org.openarchives.oai._2.OAIPMH;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
public class ListSetsResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle(@Form ListSetsRequest listSetsRequest) {
        OAIPMH rootType = createRootType();
        ListSetsType listSetsType;

        try {
            if (listSetsRequest.getResumptionToken() != null) {
                throw new BadResumptionTokenPmhException();
            } else {
                listSetsType = oaiConfiguration.listSets();
            }
            rootType.setListSets(listSetsType);
        } catch (NoSetHierarchyPmhException | BadResumptionTokenPmhException e) {
            rootType.getErrors().add(e.getOAIPMHerrorType());
        }
        return rootType;
    }
}
