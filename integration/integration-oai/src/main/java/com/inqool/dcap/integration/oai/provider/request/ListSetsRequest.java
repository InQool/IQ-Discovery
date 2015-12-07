package com.inqool.dcap.integration.oai.provider.request;

import javax.ws.rs.QueryParam;

public class ListSetsRequest {
    @QueryParam("resumptionToken")
	private String resumptionToken;
	
	public String getResumptionToken() {
		return resumptionToken;
	}
	public void setResumptionToken(String resumptionToken) {
		this.resumptionToken = resumptionToken;
	}
}
