package com.inqool.dcap.integration.z3950.sru.server.request;


import javax.ws.rs.QueryParam;

public class SearchRetrieveRequest {
    @QueryParam("version")
    private String version;

    @QueryParam("query")
	private String query;

    @QueryParam("recordSchema")
    private String recordSchema;

    @QueryParam("startRecord")
    private String startRecord;

    @QueryParam("maximumRecords")
    private String maximumRecords;

    @QueryParam("stylesheet")
    private String stylesheet;

    @QueryParam("recordPacking")
    private String recordPacking;

    @QueryParam("resultSetTTL")
    private String resultSetTTL;

    @QueryParam("sortKeys")
    private String sortKeys;

    @QueryParam("facetLimit")
    private String facetLimit;

    @QueryParam("facetStart")
    private String facetStart;

    @QueryParam("facetSort")
    private String facetSort;

    @QueryParam("facetCount")
    private String facetCount;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

    public String getRecordSchema() {
        return recordSchema;
    }

    public void setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
    }

    public String getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(String startRecord) {
        this.startRecord = startRecord;
    }

    public String getMaximumRecords() {
        return maximumRecords;
    }

    public void setMaximumRecords(String maximumRecords) {
        this.maximumRecords = maximumRecords;
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

    public String getResultSetTTL() {
        return resultSetTTL;
    }

    public void setResultSetTTL(String resultSetTTL) {
        this.resultSetTTL = resultSetTTL;
    }

    public String getSortKeys() {
        return sortKeys;
    }

    public void setSortKeys(String sortKeys) {
        this.sortKeys = sortKeys;
    }

    public String getFacetLimit() {
        return facetLimit;
    }

    public void setFacetLimit(String facetLimit) {
        this.facetLimit = facetLimit;
    }

    public String getFacetStart() {
        return facetStart;
    }

    public void setFacetStart(String facetStart) {
        this.facetStart = facetStart;
    }

    public String getFacetSort() {
        return facetSort;
    }

    public void setFacetSort(String facetSort) {
        this.facetSort = facetSort;
    }

    public String getFacetCount() {
        return facetCount;
    }

    public void setFacetCount(String facetCount) {
        this.facetCount = facetCount;
    }
}
