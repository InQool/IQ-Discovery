package com.inqool.dcap.ip;

import com.inqool.dcap.common.dto.PdfCreatorDto;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created by John on 6.7.2015.
 *
 * This code was created by Kapurka and every bit of sanity found here was added by Lukess.
 */
@Path("/pdf/")
@RequestScoped
public class PDFCreatorRS {

    @Inject
    private PDFCreator pdfCreator;

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void process(PdfCreatorDto pdfCreatorDto) {
        pdfCreator.createPdf(pdfCreatorDto);
    }
}
