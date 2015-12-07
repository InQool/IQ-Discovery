package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.MailNotification;
import com.inqool.dcap.mail.MailSender;
import com.inqool.dcap.mail.TemplateService;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
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

    public void notifyBatchHandedOver(String firstName, String lastName, String batchesList, String mail) throws IOException {
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("batch_owner_changed");

        String templateText = mailNotification.getText()
                .replace("#jméno_dávky", batchesList)
                .replace("#jméno", firstName)
                .replace("#příjmení", lastName);

        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, mail, mailNotification.getSubject(), out.toString("utf-8"));
    }

    public void notifyUserVerified(String firstName, String lastName, String mail) throws IOException {
        MailNotification mailNotification = mailNotificationAccess.fetchMailNotification("discovery_user_verified");

        String templateText = mailNotification.getText()
                .replace("#jméno", firstName)
                .replace("#příjmení", lastName);

        Map<String, Object> entries = new HashMap<>();

        entries.put("text", templateText);
        addFooter(entries);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        templateService.applyTemplate("standardTemplate", entries, out);
        mailSender.sendMail(from, mail, mailNotification.getSubject(), out.toString("utf-8"));
    }

    private void addFooter(Map<String, Object> entries) {
        entries.put("footer", "Tento mail je automaticky generován systémem " + portalSettingsAccess.fetchPortalSettings().getDiscoveryTitle() + ".");
    }
}
