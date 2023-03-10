package fi.vm.yti.groupmanagement.service;

import java.util.List;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.groupmanagement.config.ApplicationProperties;
import fi.vm.yti.groupmanagement.dao.EmailSenderDao;
import fi.vm.yti.groupmanagement.dao.FrontendDao;
import fi.vm.yti.groupmanagement.model.TempUser;
import fi.vm.yti.groupmanagement.model.UnsentRequestsForOrganization;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;

@Service
public class EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
    private final ApplicationProperties applicationProperties;
    private final EmailSenderDao emailSenderDao;
    private final FrontendDao frontendDao;
    private final JavaMailSender javaMailSender;
    private final String environmentUrl;
    private final String adminEmail;

    @Autowired
    public EmailSenderService(final ApplicationProperties applicationProperties,
                              final EmailSenderDao emailSenderDao,
                              final FrontendDao frontendDao,
                              final JavaMailSender javaMailSender,
                              @Value("${environment.url}") final String environmentUrl,
                              @Value("${admin.email}") final String adminEmail) {
        this.applicationProperties = applicationProperties;
        this.emailSenderDao = emailSenderDao;
        this.frontendDao = frontendDao;
        this.javaMailSender = javaMailSender;
        this.environmentUrl = environmentUrl;
        this.adminEmail = adminEmail;
        logger.info("Use configured ADMIN email: " + adminEmail);
    }

    private static Address createAddress(final String address) {
        try {
            return new InternetAddress(address);
        } catch (AddressException e) {
            logger.error("createAddress failed for address: " + address);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public int sendEmailsToTempUsersWithContainer(final String containerUri) {
        logger.debug("Sending invitations to temp users with containerUri: " + containerUri);
        final List<TempUser> tempUsers = emailSenderDao.getTempUsersWithoutTokensAndContainerUri(containerUri);
        for (final TempUser tempUser : tempUsers) {
            final String token = frontendDao.createToken(tempUser.id, "tempuser");
            final String uri = constructContainerUriWithTokenAndEnv(tempUser.containerUri, token);
            sendTempUserInvitationEmail(tempUser.email, tempUser.id, tempUser.containerUri, uri);
        }
        return tempUsers.size();
    }

    @Transactional
    public void sendEmailsToAdmins() {
        for (final UnsentRequestsForOrganization request : emailSenderDao.getUnsentRequests()) {
            sendAccessRequestEmail(request.adminEmails, request.requestCount, request.name);
            emailSenderDao.markRequestAsSentForOrganization(request.id);
        }
    }

    @Transactional
    public void sendEmailToUserOnAcceptance(final String userEmail,
                                            final UUID userId,
            final String organizationName) {
        sendAccessRequestAcceptedEmail(userEmail, userId, organizationName);
    }

    private void sendAccessRequestEmail(final List<String> adminEmails,
                                        final int requestCount,
            final String organizationName) {
        try {
            final MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipients(BCC, adminEmails.stream().map(EmailSenderService::createAddress).toArray(Address[]::new));
            final String message = "Sinulle on " + requestCount + " uutta k??ytt??oikeuspyynt???? organisaatioon '"
                    + organizationName + "':   " + environmentUrl;
            mail.setFrom(createAddress(adminEmail));
            mail.setSender(createAddress(adminEmail));
            mail.setSubject("Sinulle on uusia k??ytt??oikeuspyynt??j??", "UTF-8");
            mail.setText(message, "UTF-8");
            javaMailSender.send(mail);
        } catch (final MessagingException e) {
            logger.warn("sendAccessRequestEmail failed for organization: " + organizationName);
            throw new RuntimeException(e);
        }
    }

    private void sendAccessRequestAcceptedEmail(final String userEmail,
                                                final UUID userId,
            final String organizationName) {
        try {
            final MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipient(TO, createAddress(userEmail));
            final String message = "Teille on my??nnetty k??ytt??oikeus yhteentoimivuusalustan organisaatioon '"
                    + organizationName + "':   " + environmentUrl;
            mail.setFrom(createAddress(adminEmail));
            mail.setSender(createAddress(adminEmail));
            mail.setSubject("Ilmoitus k??ytt??oikeuden hyv??ksymisest??", "UTF-8");
            mail.setText(message, "UTF-8");
            javaMailSender.send(mail);
            logger.info("Organization request accepted email sent to: " + userId);
        } catch (final MessagingException e) {
            logger.warn("sendAccessRequestAcceptedEmail failed for organization: " + organizationName);
            // Just consume exception if mail sending fails. But add log message
            //            throw new RuntimeException(e);
        }
    }

    private void sendTempUserInvitationEmail(final String userEmail,
                                             final UUID userId,
                                             final String resourceUri,
                                             final String resourceUriWithToken) {
        try {
            final MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipient(TO, createAddress(userEmail));
            final String message = createTempUserInviteString(resourceUri, resourceUriWithToken);
            mail.setFrom(createAddress(adminEmail));
            mail.setSender(createAddress(adminEmail));
            mail.setSubject("Ilmoitus kommentointikierrokselle osallistumisesta", "UTF-8");
            mail.setContent(message, "text/html; charset=UTF-8");
            javaMailSender.send(mail);
            logger.info("Temp user invitation email sent to: " + userId + " for resource: " + resourceUri);
        } catch (final MessagingException e) {
            logger.warn("sendTempUserInvitationEmail failed for user: " + userId);
        }
    }

    private String createTempUserInviteString(final String resourceUri,
                                              final String resourceUriWithToken) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<body>");
        stringBuffer.append("Hyv?? vastaanottaja,<br/><br/>");
        stringBuffer.append("Olet saanut kutsun Yhteentoimivuusalustan kommentointikierrokselle.<br/><br/>");
        stringBuffer.append("P????set kommentoimaan kierroksella olevia sis??lt??j?? alla olevasta linkist??. Huomioithan, ett?? linkki on henkil??kohtainen ja sen kautta tehty kommentointi tallentuu kierrokselle sinun nimiss??si! ??l?? siis jaa linkki?? eteenp??in. Linkki on voimassa 6 kk.<br/><br/>");
        stringBuffer.append("<a href=\"" + resourceUriWithToken + "\">" + resourceUri + "</a><br/><br/>");
        stringBuffer.append("Kommentointi-ty??kalun k??ytt??ohjeet l??yd??t <a href=\"https://wiki.dvv.fi/display/YTIJD/6.+Kommentointi\">t????lt??</a>.<br/><br/>");
        stringBuffer.append("T??m?? viesti on l??hetetty automaattisesti Yhteentoimivuusalustan Kommentit-ty??kalusta. Eth??n vastaa viestiin!<br/><br/>");
        stringBuffer.append("Jos k??yt??n suhteen ilmenee teknisi?? ongelmia, otathan yhteytt?? Digi- ja v??est??tietovirastoon yhteentoimivuus@dvv.fi!");
        stringBuffer.append("</body>");
        return stringBuffer.toString();
    }

    private String constructContainerUriWithTokenAndEnv(final String uri,
                                                        final String token) {
        final String env = applicationProperties.getEnv();
        if ("prod".equalsIgnoreCase(env)) {
            return uri + "?token=" + token;
        } else {
            return uri + "?env=" + env + "&token=" + token;
        }
    }
}
