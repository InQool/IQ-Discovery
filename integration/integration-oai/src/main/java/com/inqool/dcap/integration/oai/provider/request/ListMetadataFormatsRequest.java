package com.inqool.dcap.integration.oai.provider.request;

import javax.ws.rs.QueryParam;

public class ListMetadataFormatsRequest {
    @QueryParam("identifier")
	private String identifier;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
