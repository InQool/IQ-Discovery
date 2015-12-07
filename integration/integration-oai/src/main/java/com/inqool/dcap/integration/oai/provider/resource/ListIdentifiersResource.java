package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.formats.FormatDescriptor;
import com.inqool.dcap.integration.oai.provider.exception.CannotDisseminateFormatPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoRecordsMatchPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoSetHierarchyPmhException;
import com.inqool.dcap.integration.oai.provider.request.ListIdentifiersRequest;
import org.jboss.resteasy.annotations.Form;
import org.openarchives.oai._2.ListIdentifiersType;
import org.openarchives.oai._2.OAIPMH;
import org.openarchives.oai._2.OAIPMHerrorType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

@RequestScoped
public class ListIdentifiersResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle(@Form ListIdentifiersRequest listIdentifiersRequest) {
        OAIPMH rootType = createRootType();
        OffsetDateTime from;
        OffsetDateTime until;
        String resumptionToken;
        int page = 0;
        //Parse info from resumption token
        if ((resumptionToken = listIdentifiersRequest.getResumptionToken()) != null) {
            if (listIdentifiersRequest.getFrom() != null || listIdentifiersRequest.getUntil() != null || listIdentifiersRequest.getSet() != null || listIdentifiersRequest.getMetadataPrefix() != null) {
                OAIPMHerrorType error = new OAIPMHerrorType();
                error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
                error.setValue("Resumption token is an exclusive argument!");
                rootType.getErrors().add(error);
                return rootType;
            }
            List<String> tokenParts = Arrays.asList(resumptionToken.split("[.]"));
            if (tokenParts.size() != 5) {
                OAIPMHerrorType error = new OAIPMHerrorType();
                error.setCode(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
                error.setValue("Bad resumption token!");
                rootType.getErrors().add(error);
                return rootType;
            }
            //Set null instead of empty strings
            for(int i=0; i<tokenParts.size(); i++) {
                if("".equals(tokenParts.get(i))) {
                    tokenParts.set(i, null);
                }
            }
            listIdentifiersRequest.setFrom(tokenParts.get(0));
            listIdentifiersRequest.setUntil(tokenParts.get(1));
            listIdentifiersRequest.setMetadataPrefix(tokenParts.get(2));
            listIdentifiersRequest.setSet(tokenParts.get(3));

            try {
                page = Integer.valueOf(tokenParts.get(4));
            } catch (Exception e) {
                OAIPMHerrorType error = new OAIPMHerrorType();
                error.setCode(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
                error.setValue("Bad resumption token!");
                rootType.getErrors().add(error);
                return rootType;
            }
        }

        if(listIdentifiersRequest.getMetadataPrefix() == null) {
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
            error.setValue("A metadataPrefix parameter must be specified!");
            rootType.getErrors().add(error);
            return rootType;
        }
        if((listIdentifiersRequest.getUntil() != null && listIdentifiersRequest.getUntil().contains("T"))
                || (listIdentifiersRequest.getFrom() != null && listIdentifiersRequest.getFrom().contains("T"))) {
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
            error.setValue("Only YYYY-MM-DD granularity supported!");
            rootType.getErrors().add(error);
            return rootType;
        }

        if(listIdentifiersRequest.getFrom() == null) {
            from = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        }
        else {
            from = DatatypeConverter.parseDateTime(listIdentifiersRequest.getFrom()).toInstant().atOffset(ZoneOffset.UTC);
        }
        if(listIdentifiersRequest.getUntil() == null) {
            until = OffsetDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        }
        else {
            until = DatatypeConverter.parseDateTime(listIdentifiersRequest.getUntil()).toInstant().atOffset(ZoneOffset.UTC);
        }

        ListIdentifiersType listIdentifiersType;
        try {
            if(listIdentifiersRequest.getSet() != null) {
                throw new NoSetHierarchyPmhException();
            }
            String metadataPrefix = listIdentifiersRequest.getMetadataPrefix();
            FormatDescriptor formatDescriptor = oaiConfiguration.getFormatDescriptor(metadataPrefix);
            if(formatDescriptor == null) throw new CannotDisseminateFormatPmhException("Metadata format " + metadataPrefix + " not supported.");

            listIdentifiersType = formatDescriptor.listIdentifiers(from, until,
                    listIdentifiersRequest.getSet(), page);
            rootType.setListIdentifiers(listIdentifiersType);
        } catch (NoSetHierarchyPmhException | NoRecordsMatchPmhException | CannotDisseminateFormatPmhException e) {
            rootType.getErrors().add(e.getOAIPMHerrorType());
        }
        return rootType;
    }
}
