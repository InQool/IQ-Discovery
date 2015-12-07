package com.inqool.dcap.integration.oai.provider.request;


import javax.ws.rs.QueryParam;

public class ListRecordsRequest {
    @QueryParam("from")
	private String from;

    @QueryParam("until")
	private String until;

    @QueryParam("set")
	private String set;

    @QueryParam("resumptionToken")
	private String resumptionToken;

    @QueryParam("metadataPrefix")
	private String metadataPrefix;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getUntil() {
		return until;
	}

	public void setUntil(String until) {
		this.until = until;
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}

	public String getResumptionToken() {
		return resumptionToken;
	}

	public void setResumptionToken(String resumptionToken) {
		this.resumptionToken = resumptionToken;
	}

	public String getMetadataPrefix() {
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}

}
