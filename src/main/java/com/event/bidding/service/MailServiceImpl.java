package com.event.bidding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendSimpleMail(String to, String subject, String body) {
        try {
            log.info("=== EMAIL SEND ATTEMPT ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body: {}", body);
            log.info("Preparing to send email to: {}, subject: {}", to, subject);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            msg.setFrom("info@tconsolutions.com"); // Set the from address

            log.info("Sending email via JavaMailSender...");
            mailSender.send(msg);
            log.info("=== EMAIL SENT SUCCESSFULLY to: {} ===", to);
        } catch (Exception e) {
            log.error("=== EMAIL SEND FAILED ===");
            log.error("To: {}", to);
            log.error("Subject: {}", subject);
            log.error("Error message: {}", e.getMessage());
            log.error("Error type: {}", e.getClass().getName());
            log.error("Full stack trace:", e);
            log.error("=== END EMAIL ERROR ===");
            // Throw the exception so caller knows it failed
            throw new RuntimeException("Failed to send email to " + to + ": " + e.getMessage(), e);
        }
    }
}

