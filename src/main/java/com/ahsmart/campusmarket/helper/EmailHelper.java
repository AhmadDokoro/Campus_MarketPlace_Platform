package com.ahsmart.campusmarket.helper;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * This class is responsible for sending emails using Gmail's SMTP server.
 * It uses Jakarta Mail API to configure and send email messages securely.
 */
@Component
public class EmailHelper {

    private final String systemEmail = "campus.marketplace.umt@gmail.com";
    private final String appPassword = "lemj mynf vrye qlsg";

    public void sendEmail(String recipientEmail, String subject, String body) {
        // Set up properties for the email session
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // Enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS encryption
        props.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP server
        props.put("mail.smtp.port", "587"); // SMTP port for TLS

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Provide system email and app password for authentication
                return new PasswordAuthentication(systemEmail, appPassword);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(systemEmail)); // Set sender email
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail)); // Set recipient email
            message.setSubject(subject); // Set email subject
            message.setText(body); // Set email body

            // Send the email
            Transport.send(message);
        } catch (MessagingException e) {
            // Handle any errors during the email sending process
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
