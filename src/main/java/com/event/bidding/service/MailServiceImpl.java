package com.event.bidding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:info@tconsolutions.com}")
    private String mailFrom;

    @Override
    public void sendSimpleMail(String to, String subject, String body) {
        try {
            log.info("=== EMAIL SEND ATTEMPT ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body: {}", body);
            log.info("Preparing to send email to: {}, subject: {}", to, subject);

            // Log the actual JavaMailSender implementation class to help diagnose stub vs real sender
            if (mailSender != null) {
                log.info("JavaMailSender implementation: {}", mailSender.getClass().getName());
            } else {
                log.warn("JavaMailSender bean is null");
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            // Use configured mail username as 'from' if available
            if (mailFrom != null && !mailFrom.isEmpty()) {
                msg.setFrom(mailFrom);
            } else {
                msg.setFrom("info@tconsolutions.com");
            }

            log.info("Sending email via JavaMailSender (from={})...", msg.getFrom());
            // Simple retry once for transient failures
            int attempts = 0;
            boolean sent = false;
            while (attempts < 2 && !sent) {
                attempts++;
                try {
                    mailSender.send(msg);
                    sent = true;
                    log.info("=== EMAIL SENT SUCCESSFULLY to: {} (attempt {}) ===", to, attempts);
                } catch (Exception innerEx) {
                    log.error("Attempt {} to send email failed: {}", attempts, innerEx.getMessage());
                    if (attempts >= 2) {
                        throw innerEx;
                    }
                    // small backoff
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        } catch (Exception e) {
            log.error("=== EMAIL SEND FAILED ===");
            log.error("To: {}", to);
            log.error("Subject: {}", subject);
            log.error("Error message: {}", e.getMessage());
            log.error("Error type: {}", e.getClass().getName());
            log.error("Full stack trace:", e);
            // Throw the exception so caller knows it failed
            throw new RuntimeException("Failed to send email to " + to + ": " + e.getMessage(), e);
        }
    }
}
