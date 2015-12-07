package com.inqool.dcap.integration.z3950.sru.server;

import com.inqool.dcap.integration.z3950.sru.server.resource.ExplainResource;
import com.inqool.dcap.integration.z3950.sru.server.resource.OperationResource;
import com.inqool.dcap.integration.z3950.sru.server.resource.ScanResource;
import com.inqool.dcap.integration.z3950.sru.server.resource.SearchRetrieveResource;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
@RequestScoped
@Path("/sru")
public class SruResource {

/*    @Inject
    private HttpServletRequest request;*/

    @Inject
    private ExplainResource explainResource;

    @Inject
    private ScanResource scanResource;

    @Inject
    private SearchRetrieveResource searchRetrieveResource;

    @Path("/")
    public OperationResource request(@QueryParam("operation") String operationString) throws Exception {
        Verb operation;
        try {
            operation = Verb.valueOf(operationString);
        } catch (IllegalArgumentException | NullPointerException e) {
            return explainResource;
        }
        switch (operation) {
            case explain:
                return explainResource;
            case scan:
                return scanResource;
            case searchRetrieve:
                return searchRetrieveResource;
            default:
                return explainResource;
        }
    }

    public static enum Verb {
        explain,
        scan,
        searchRetrieve
    }
}
