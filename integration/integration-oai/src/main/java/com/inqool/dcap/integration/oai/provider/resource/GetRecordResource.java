package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.formats.FormatDescriptor;
import com.inqool.dcap.integration.oai.provider.exception.CannotDisseminateFormatPmhException;
import com.inqool.dcap.integration.oai.provider.exception.IdDoesNotExistPmhException;
import com.inqool.dcap.integration.oai.provider.request.GetRecordRequest;
import org.jboss.resteasy.annotations.Form;
import org.openarchives.oai._2.GetRecordType;
import org.openarchives.oai._2.OAIPMH;
import org.openarchives.oai._2.OAIPMHerrorType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
public class GetRecordResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle(@Form GetRecordRequest getRecordRequest) {
        OAIPMH rootType = createRootType();
        String repositoryName = getRepositoryName();

        if(getRecordRequest.getIdentifier() == null) {
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
            error.setValue("An identifier parameter must be specified!");
            rootType.getErrors().add(error);
            return rootType;
        }
        if(getRecordRequest.getMetadataPrefix() == null) {
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
            error.setValue("A metadataPrefix parameter must be specified!");
            rootType.getErrors().add(error);
            return rootType;
        }

        try {
            String metadataPrefix = getRecordRequest.getMetadataPrefix();
            FormatDescriptor formatDescriptor = oaiConfiguration.getFormatDescriptor(metadataPrefix);
            if(formatDescriptor == null) throw new CannotDisseminateFormatPmhException("Metadata format " + metadataPrefix + " not supported.");

            GetRecordType getRecordType = formatDescriptor.getRecord(getRecordRequest.getIdentifier());
            rootType.setGetRecord(getRecordType);
        } catch (IdDoesNotExistPmhException | CannotDisseminateFormatPmhException e) {
            rootType.getErrors().add(e.getOAIPMHerrorType());
        }
        return rootType;
    }
}
