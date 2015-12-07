package com.inqool.dcap.integration.oai.provider.exception;

import org.openarchives.oai._2.OAIPMHerrorcodeType;

public class IdDoesNotExistPmhException extends PmhException {

	public IdDoesNotExistPmhException() {
		super(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, "Specified ID doesn't exist in this repository!");
	}

}
