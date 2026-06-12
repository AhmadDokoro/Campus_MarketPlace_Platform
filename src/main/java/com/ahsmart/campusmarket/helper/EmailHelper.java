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
        if (recipientEmail == null || recipientEmail.isBlank()) {
            logger.error("Cannot send email '{}': recipient address is missing", subject);
            return;
        }

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
            logger.info("Email '{}' sent to {}", subject, recipientEmail);
        } catch (Exception e) {
            // Catch everything (not just MessagingException): this runs on a background
            // thread, so this log line is the only visible signal when an email fails.
            // Log the full stack trace instead of swallowing the cause.
            logger.error("Failed to send email '{}' to {}", subject, recipientEmail, e);
        }
    }
}
