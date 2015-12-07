package com.inqool.dcap.mail;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.mail.Session;

@ApplicationScoped
public class SessionProducer {
    @Resource(mappedName="java:jboss/mail/Default")
	@Produces
	private Session mailSession;
}
