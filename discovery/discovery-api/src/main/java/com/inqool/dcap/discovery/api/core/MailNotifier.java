package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.MailNotification;
import com.inqool.dcap.mail.MailSender;
import com.inqool.dcap.mail.TemplateService;
import com.inqool.dcap.security.model.DiscoveryUser;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 24. 8. 2015.
 */
@ApplicationScoped
public class MailNotifier {
    @Inject
    private TemplateService templateService;

    @Inject
    private MailSender mailSender;

    @Inject
    private MailNotificationAccess mailNotificationAccess;

    @Inject
    private PortalSettingsAccess portalSettingsAccess;

    @Inject
    @ConfigProperty(name = "mail.notification.from")
    private String from;

    @Inject
    @ConfigProperty(name = "discovery.web.endpoint")
    private String discoveryWebEndpoint;

    public void notifyUserRegistered(String firstName, String lastName, String userName) throws IOException {
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("discovery_user_registration");

        String templateText = mailNotification.getText()
                .replace("#jméno_uživatele", userName)
                .replace("#jméno", firstName)
                .replace("#příjmení", lastName);


        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, userName, mailNotification.getSubject(), out.toString("utf-8"));
    }

    public void notifyUserDeleted(String firstName, String lastName, String userName) throws IOException {
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("discovery_user_deleted");

        String templateText = mailNotification.getText()
                .replace("#jméno_uživatele", userName)
                .replace("#jméno", firstName)
                .replace("#příjmení", lastName);

        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, userName, mailNotification.getSubject(), out.toString("utf-8"));
    }

    public void notifyPasswordReset(String firstName, String lastName, String userName, String pwdResetKey) throws IOException {
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("password_reset");

        String url = discoveryWebEndpoint + "/password/reset?hash=" + pwdResetKey;

        String link = "<a href=\"" + url + "\">" + url + "</a>";

        String templateText = mailNotification.getText()
                .replace("#jméno_uživatele", userName)
                .replace("#jméno", firstName)
                .replace("#příjmení", lastName)
                .replace("#odkaz", link);

        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, userName, mailNotification.getSubject(), out.toString("utf-8"));
    }

    public void notifyClientDocumentsReserved(DiscoveryUser discoveryUser, List<String> docInvIds, String reason) throws IOException {
        String docInvIdsStr = StringUtils.join(docInvIds, ", ");

        //Mail the person who requested the document
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("doc_reservation_client");
        String templateText = mailNotification.getText()
                .replace("#dokument", docInvIdsStr)
                .replace("#účel", reason)
                .replace("#jméno", discoveryUser.getFirstName())
                .replace("#příjmení", discoveryUser.getLastName());

        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, discoveryUser.getEmail(), mailNotification.getSubject().replace("#dokument", docInvIdsStr), out.toString("utf-8"));
    }

    public void notifyOwnerDocumentReserved(DiscoveryUser discoveryUser, List<String> docInvIds, String reason, String ownerMail) throws IOException {
        String docInvIdsStr = StringUtils.join(docInvIds, ", ");

        //Mail the owner of the document
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("doc_reservation_owner");

        String address = discoveryUser.getStreet() + " " + discoveryUser.getStreetNumber() + ", " + discoveryUser.getCity() + ", " + discoveryUser.getZip();

        String templateText = mailNotification.getText()
                .replace("#jméno_uživatele", discoveryUser.getLoginName())
                .replace("#dokument", docInvIdsStr)
                .replace("#adresa", address)
                .replace("#účel", reason)
                .replace("#jméno", discoveryUser.getFirstName())
                .replace("#příjmení", discoveryUser.getLastName());

        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, ownerMail, mailNotification.getSubject().replace("#dokument", docInvIdsStr), out.toString("utf-8"));
    }

    public void notifyOwnerDocumentQuery(DiscoveryUser discoveryUser, String invId, String docQuery, String ownerEmail) throws IOException {
        String subject = "Dotaz na dokument " + invId;
        String templateText = "Uživatel " + discoveryUser.getFirstName() + " " + discoveryUser.getLastName() +
                "má následující dotaz k dokumentu " + invId + ":\n\n" + docQuery + "\n\n Uživateli lze odpovědět na adresu " + discoveryUser.getEmail();
        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, ownerEmail, subject, out.toString("utf-8"), discoveryUser.getEmail()); /*discoveryUser.getEmail()*/
    }

    private void addFooter(Map<String, Object> entries) {
        entries.put("footer", "Tento mail je automaticky generován systémem " + portalSettingsAccess.fetchPortalSettings().getDiscoveryTitle() + ".");
    }
}
