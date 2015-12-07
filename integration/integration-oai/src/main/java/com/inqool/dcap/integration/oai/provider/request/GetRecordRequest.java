package com.inqool.dcap.integration.oai.provider.request;

import javax.ws.rs.QueryParam;

public class GetRecordRequest {
    @QueryParam("identifier")
	private String identifier;

    @QueryParam("metadataPrefix")
	private String metadataPrefix;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getMetadataPrefix() {
		return metadataPrefix;
	}
	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}
}
