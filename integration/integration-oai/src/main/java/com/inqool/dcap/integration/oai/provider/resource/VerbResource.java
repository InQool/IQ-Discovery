package com.inqool.dcap.integration.oai.provider.resource;

import com.inqool.dcap.integration.oai.provider.OaiPmhConfiguration;
import org.openarchives.oai._2.OAIPMH;
import org.openarchives.oai._2.RequestType;
import org.openarchives.oai._2.VerbType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
@RequestScoped
public class VerbResource {
    @Inject
    protected HttpServletRequest request;

    @Inject
    protected OaiPmhConfiguration oaiConfiguration;

    protected OAIPMH createRootType() {
        if (request == null) throw new IllegalArgumentException("request can't be null");

        OAIPMH rootType = new OAIPMH();
        rootType.setResponseDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        RequestType requestType = new RequestType();
        if( request.getAttribute("originalURL")!=null){
            requestType.setValue(request.getAttribute("originalURL").toString());
        }else if ( request.getRequestURL()!=null){
            requestType.setValue(request.getRequestURL().toString());
        }

        requestType.setIdentifier(request.getParameter("identifier"));
        requestType.setMetadataPrefix(request.getParameter("metadataPrefix"));
        requestType.setResumptionToken(request.getParameter("resumptionToken"));
        requestType.setSet(request.getParameter("set"));

        try {
            DatatypeConverter.parseDateTime(request.getParameter("from"));
            requestType.setFrom(request.getParameter("from"));
        } catch (Exception ignored) {}

        try {
            DatatypeConverter.parseDateTime(request.getParameter("until"));
            requestType.setUntil(request.getParameter("until"));
        } catch (Exception ignored) {}

        try {
            requestType.setVerb(VerbType.fromValue(request.getParameter("verb")));
        } catch (IllegalArgumentException e) {
            // Bad verb
        }
        rootType.setRequest(requestType);
        return rootType;
    }

    protected String getRepositoryName() {
        return "/".equals(request.getPathInfo()) ? null : request.getPathInfo();
    }
}
