package com.inqool.dcap.integration.oai.provider.exception;

import org.openarchives.oai._2.OAIPMHerrorcodeType;

public class NoRecordsMatchPmhException extends PmhException {

	public NoRecordsMatchPmhException() {
		super(OAIPMHerrorcodeType.NO_RECORDS_MATCH, "The combination of the values of the from, until, and set arguments results in an empty list!");
	}

}
