package com.ahsmart.campusmarket.helper;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EmailHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailHelper.class);

    private final String systemEmail = "campus.marketplace.umt@gmail.com";
    private final String appPassword = "lemj mynf vrye qlsg";

    // Runs in a background thread so it never blocks the HTTP request.
    @Async
    public void sendEmail(String recipientEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "20000"); // 20 s connect timeout
        props.put("mail.smtp.timeout", "20000");           // 20 s read timeout
        props.put("mail.smtp.writetimeout", "20000");      // 20 s write timeout

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(systemEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(systemEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
        }
    }
}
