package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.formats.FormatDescriptor;
import com.inqool.dcap.integration.oai.provider.exception.CannotDisseminateFormatPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoRecordsMatchPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoSetHierarchyPmhException;
import com.inqool.dcap.integration.oai.provider.request.ListRecordsRequest;
import org.jboss.resteasy.annotations.Form;
import org.openarchives.oai._2.ListRecordsType;
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
public class ListRecordsResource extends VerbResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OAIPMH handle(@Form ListRecordsRequest listRecordsRequest) {
        OAIPMH rootType = createRootType();
        OffsetDateTime from;
        OffsetDateTime until;
        String resumptionToken;
        int page = 0;
        //Parse info from resumption token
        if ((resumptionToken = listRecordsRequest.getResumptionToken()) != null) {
            if (listRecordsRequest.getFrom() != null || listRecordsRequest.getUntil() != null || listRecordsRequest.getSet() != null || listRecordsRequest.getMetadataPrefix() != null) {
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
            listRecordsRequest.setFrom(tokenParts.get(0));
            listRecordsRequest.setUntil(tokenParts.get(1));
            listRecordsRequest.setMetadataPrefix(tokenParts.get(2));
            listRecordsRequest.setSet(tokenParts.get(3));

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
        if (listRecordsRequest.getMetadataPrefix() == null) {
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
            error.setValue("A metadataPrefix parameter must be specified!");
            rootType.getErrors().add(error);
            return rootType;
        }
        if ((listRecordsRequest.getUntil() != null && listRecordsRequest.getUntil().contains("T"))
                || (listRecordsRequest.getFrom() != null && listRecordsRequest.getFrom().contains("T"))) {
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
            error.setValue("Only YYYY-MM-DD granularity supported!");
            rootType.getErrors().add(error);
            return rootType;
        }

        if (listRecordsRequest.getFrom() == null) {
            from = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        } else {
            from = DatatypeConverter.parseDateTime(listRecordsRequest.getFrom()).toInstant().atOffset(ZoneOffset.UTC);
        }
        if (listRecordsRequest.getUntil() == null) {
            until = OffsetDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        } else {
            until = DatatypeConverter.parseDateTime(listRecordsRequest.getUntil()).toInstant().atOffset(ZoneOffset.UTC);
        }

        ListRecordsType listRecordsType;
        try {
            if(listRecordsRequest.getSet() != null) {
                throw new NoSetHierarchyPmhException();
            }

            String metadataPrefix = listRecordsRequest.getMetadataPrefix();
            FormatDescriptor formatDescriptor = oaiConfiguration.getFormatDescriptor(metadataPrefix);
            if(formatDescriptor == null) throw new CannotDisseminateFormatPmhException("Metadata format " + metadataPrefix + " not supported.");

            listRecordsType = formatDescriptor.listRecords(from, until, listRecordsRequest.getSet(), page);
            rootType.setListRecords(listRecordsType);
        } catch (NoSetHierarchyPmhException | NoRecordsMatchPmhException | CannotDisseminateFormatPmhException e) {
            rootType.getErrors().add(e.getOAIPMHerrorType());
        }
        return rootType;
    }
}
