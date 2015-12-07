package com.inqool.dcap.mail;

import com.inqool.dcap.config.Zdo;
import org.slf4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Matus
 * Date: 29.10.2013
 * Time: 11:24
 */
@ApplicationScoped
public class MailSender {

	@Inject
    @Zdo
	private Logger logger;

	@Inject
	private Session mailSession;

    public void sendMail(String from, String to, String subject, String body) throws IOException {
        this.sendMail(from, to, subject, body, null, null);
    }

    public void sendMail(String from, String to, String subject, String body, String replyTo) throws IOException {
        this.sendMail(from, to, subject, body, null, replyTo);
    }

    public void sendMail(String from, String to, String subject, String body, Map<String, InputStream> attachments) throws IOException {
        sendMail(from, to, subject, body, attachments, null);
    }

    public void sendMail(String from, String to, String subject, String body, Map<String, InputStream> attachments, String replyTo) throws IOException {
        checkNotNull(from);
        checkNotNull(to);

        try {
            MimeMessage msg = new MimeMessage(mailSession);

            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress(from));
            if(replyTo != null) {
                msg.setReplyTo(new InternetAddress[] {new InternetAddress(replyTo)});
            }

            // creates body part for the message
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html;charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            if (attachments != null) {
                for (Map.Entry<String, InputStream> entry : attachments.entrySet()) {
                    // attachment
                    MimeBodyPart part = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(entry.getValue(), MediaType.APPLICATION_OCTET_STREAM);
                    part.setDataHandler(new DataHandler(source));
                    part.setFileName(entry.getKey());
                    multipart.addBodyPart(part);
                }
            }

            msg.setContent(multipart);

            mailSession.getProperties().setProperty("mail.smtp.ssl.trust", "*");

            Transport.send(msg);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }
    }
}
