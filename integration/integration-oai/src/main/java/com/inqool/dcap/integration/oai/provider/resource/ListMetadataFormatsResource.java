package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.formats.FormatDescriptor;
import com.inqool.dcap.integration.oai.provider.request.ListMetadataFormatsRequest;
import org.jboss.resteasy.annotations.Form;
import org.openarchives.oai._2.ListMetadataFormatsType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.OAIPMH;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
public class ListMetadataFormatsResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle(@Form ListMetadataFormatsRequest listMetadataFormatsRequest) {
        OAIPMH rootType = createRootType();
        ListMetadataFormatsType listMetadataFormatsType;

        //listMetadataFormatsRequest.getIdentifier(); todo list formats for specific identifiers?

        listMetadataFormatsType = new ListMetadataFormatsType();
        for(FormatDescriptor formatDescriptor : oaiConfiguration.listAllFormats()) {
            MetadataFormatType metadataFormatType = new MetadataFormatType();
            metadataFormatType.setMetadataPrefix(formatDescriptor.getMetadataPrefix());
            metadataFormatType.setMetadataNamespace(formatDescriptor.getMetadataNamespace());
            metadataFormatType.setSchema(formatDescriptor.getSchema());
            listMetadataFormatsType.getMetadataFormats().add(metadataFormatType);
        }
        rootType.setListMetadataFormats(listMetadataFormatsType);

/*        } catch (IdDoesNotExistPmhException e) {
            rootType.getErrors().add(e.getOAIPMHerrorType());
        }*/

        return rootType;
    }
}
