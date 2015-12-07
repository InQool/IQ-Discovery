package com.inqool.dcap.integration.oai.provider.exception;

import org.openarchives.oai._2.OAIPMHerrorcodeType;

public class CannotDisseminateFormatPmhException extends PmhException {

	public CannotDisseminateFormatPmhException(String message) {
		super(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, message);
	}

}
