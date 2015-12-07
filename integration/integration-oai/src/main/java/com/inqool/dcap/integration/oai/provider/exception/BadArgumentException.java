package com.inqool.dcap.integration.oai.provider.exception;

import org.openarchives.oai._2.OAIPMHerrorcodeType;

public class BadArgumentException extends PmhException {
	public BadArgumentException(String message) {
		super(OAIPMHerrorcodeType.BAD_ARGUMENT, message);
	}
}
