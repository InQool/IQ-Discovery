package com.inqool.dcap.integration.z3950.sru.server.request;

import javax.ws.rs.QueryParam;

public class ExplainRequest {
    @QueryParam("query")
    private String version;

    @QueryParam("stylesheet")
    private String stylesheet;

    @QueryParam("recordPacking")
    private String recordPacking;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public String getRecordPacking() {
        return recordPacking;
    }

    public void setRecordPacking(String recordPacking) {
        this.recordPacking = recordPacking;
    }
}
