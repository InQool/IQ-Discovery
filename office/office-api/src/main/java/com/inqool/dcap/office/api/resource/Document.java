package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.DocumentAccess;
import com.inqool.dcap.office.api.dto.DocDetailContainer;
import com.inqool.dcap.office.api.dto.ZdoDocumentBrief;
import com.inqool.dcap.office.api.dto.ZdoDocumentForExpo;
import com.inqool.dcap.office.api.request.DocumentListRequest;
import com.inqool.dcap.security.ZdoRoles;
import org.jboss.resteasy.annotations.Form;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 3. 3. 2015.
 */
@Path("/document")
@LoggedIn
@RolesAllowed(ZdoRoles.CURATOR)
public class Document {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DocumentAccess documentAccess;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDocuments(@Form DocumentListRequest request) {
        try {
            if(request.getLimit() != null && request.getLimit() == 0) {
                return Response.ok(documentAccess.countDocuments(request)).build();
            }

            List<ZdoDocumentBrief> docList = documentAccess.listDocuments(request);
            return Response.ok(docList).build();
        } catch(Exception e) {
            logger.error("Could not list documents.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/forExpo/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDocumentsForExpo(@Form DocumentListRequest request) {
        try {
            List<ZdoDocumentForExpo> docList = documentAccess.listDocumentsForExpo(request);
            return Response.ok(docList).build();
        } catch(Exception e) {
            logger.error("Could not list documents for expo.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/detail")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDocument(List<Map<String, List<String>>> propsList) {
        try {
            documentAccess.updateDocuments(propsList);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update documents.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/detail/{docId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentDetail(@PathParam("docId") String docId) {
        try {
            DocDetailContainer docDetailContainer = documentAccess.getDocumentDetail(docId);
            return Response.ok(docDetailContainer).build();
        } catch(Exception e) {
            logger.error("Could not get document detail.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    public enum DocumentState {
        original,
        concept,
        published,
        unpublished,
        discarded,
        notOriginal, //concept and published
        notConcept, //original and published
        notPublished, //concept and original
        all    //original, concept and published
    }

    public static DocumentState zdoGroupToDocumentState(String group) {
        switch(group) {
            case "ZDO":
                return Document.DocumentState.published;
            case "ZDO_CONCEPT":
                return Document.DocumentState.concept;
            case "KDR":
                return Document.DocumentState.original;
            case "UNPUBLISHED":
                return Document.DocumentState.unpublished;
            case "DISCARDED":
                return Document.DocumentState.discarded;
            default:
                return Document.DocumentState.original;
        }
    }
}
