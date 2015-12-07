package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.request.IdentifyRequest;
import org.jboss.resteasy.annotations.Form;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.OAIPMH;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
public class IdentifyResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle(@Form IdentifyRequest identifyRequest) {
        OAIPMH rootType = createRootType();
        IdentifyType identifyType = oaiConfiguration.identify();

        String url = rootType.getRequest().getValue();
        if( url.indexOf('?')>0){
            url = url.substring(0,url.indexOf('?'));
        }
        identifyType.setBaseURL(url);
        rootType.setIdentify(identifyType);

        return rootType;
    }
}
