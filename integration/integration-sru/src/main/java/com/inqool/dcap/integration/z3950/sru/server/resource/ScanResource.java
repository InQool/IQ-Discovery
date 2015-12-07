package com.inqool.dcap.integration.z3950.sru.server.resource;

import com.inqool.dcap.integration.z3950.sru.server.config.SruDiagnosticsConstants;
import com.inqool.dcap.integration.z3950.sru.server.request.ScanRequest;
import org.jboss.resteasy.annotations.Form;
import org.oasis_open.docs.ns.search_ws.diagnostic.DiagnosticComplexType;
import org.oasis_open.docs.ns.search_ws.scan.DiagnosticsDefinition;
import org.oasis_open.docs.ns.search_ws.scan.ScanResponseDefinition;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
@RequestScoped
public class ScanResource extends OperationResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public ScanResponseDefinition handle(@Form ScanRequest scanRequest) {
        ScanResponseDefinition scanResponse = new ScanResponseDefinition();
        DiagnosticsDefinition diagnostics = new DiagnosticsDefinition();
        DiagnosticComplexType diagnostic = new DiagnosticComplexType();
        diagnostic.setUri(SruDiagnosticsConstants.DIAGNOSTIC_URI_PREFIX + SruDiagnosticsConstants.UNSUPPORTED_OPERATION);
        diagnostic.setDetails("scan");
        diagnostic.setMessage("Unsupported operation.");
        diagnostics.getDiagnostic().add(diagnostic);
        scanResponse.setDiagnostics(diagnostics);
        return scanResponse;
    }
}
