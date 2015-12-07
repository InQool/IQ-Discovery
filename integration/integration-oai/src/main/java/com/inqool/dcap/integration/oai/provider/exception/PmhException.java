package com.inqool.dcap.integration.oai.provider.exception;

import org.openarchives.oai._2.OAIPMHerrorType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;

public class PmhException extends Exception {
	private static final long serialVersionUID = 1L;
	private OAIPMHerrorcodeType code;
	
	public PmhException(OAIPMHerrorcodeType code, String message) {
		super(message);
		this.code = code;
	}
	
	public OAIPMHerrorcodeType getCode() {
		return code;
	}
	
	public OAIPMHerrorType getOAIPMHerrorType() {
		OAIPMHerrorType errorType = new OAIPMHerrorType();
		errorType.setCode(code);
		errorType.setValue(getMessage());
		
		return errorType;
	}
}
