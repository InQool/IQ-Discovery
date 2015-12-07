package com.inqool.dcap.integration.oai.provider.exception;

import org.openarchives.oai._2.OAIPMHerrorcodeType;

public class NoSetHierarchyPmhException extends PmhException {

	public NoSetHierarchyPmhException() {
		super(OAIPMHerrorcodeType.NO_SET_HIERARCHY, "The repository does not support sets!");
	}

}
