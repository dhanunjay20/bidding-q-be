package com.event.bidding.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:0}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:}")
    private String sslEnable; // allow unset to auto-detect

    @Value("${spring.mail.properties.mail.debug:false}")
    private String mailDebug;

    @Bean
    public JavaMailSender javaMailSender() {
        if (host == null || host.isEmpty()) {
            log.warn("spring.mail.host not set — providing LoggingJavaMailSender stub. Configure SMTP for real email delivery.");
            return new LoggingJavaMailSender();
        }

        log.info("Configuring JavaMailSender with host={}, port={}, username={}", host, port, username);

        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        impl.setHost(host);
        if (port > 0) impl.setPort(port);
        if (username != null && !username.isEmpty()) impl.setUsername(username);
        if (password != null && !password.isEmpty()) impl.setPassword(password);
        // sensible defaults to avoid long blocking calls when SMTP is unreachable
        impl.setDefaultEncoding("UTF-8");
        impl.setProtocol("smtp");

        Properties props = impl.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth != null ? smtpAuth : "true");
        props.put("mail.debug", mailDebug != null ? mailDebug : "false");

        // Prevent the mail sender from hanging indefinitely if the SMTP server is down or unreachable
        // timeouts are in milliseconds
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        // Auto-detect SSL/STARTTLS behaviour when sslEnable isn't explicitly set
        boolean sslExplicit = sslEnable != null && !sslEnable.isEmpty();
        if (!sslExplicit) {
            if (port == 465) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "false");
            } else if (port == 587) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.ssl.enable", "false");
            } else {
                // fallback to configured starttls value (or default true)
                props.put("mail.smtp.starttls.enable", starttlsEnable != null ? starttlsEnable : "true");
            }
        } else {
            // If an explicit value is provided but the port strongly indicates SSL (465), prefer SSL
            // because many providers require SSL on 465 even if an env var was mis-set.
            if (port == 465 && !Boolean.parseBoolean(sslEnable)) {
                log.warn("Port 465 detected but mail.smtp.ssl.enable explicitly set to false — overriding to true for port 465");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "false");
            } else {
                props.put("mail.smtp.ssl.enable", sslEnable);
                props.put("mail.smtp.starttls.enable", starttlsEnable != null ? starttlsEnable : "true");
            }
        }

        // Trust the host for SSL to avoid certificate issues in many SMTP providers
        if (host != null && !host.isBlank()) {
            props.put("mail.smtp.ssl.trust", host);
        }

        // If SSL is enabled (or we're using port 465), prefer modern TLS protocols
        String sslEnabled = props.getProperty("mail.smtp.ssl.enable");
        boolean usingSsl = sslEnabled != null && Boolean.parseBoolean(sslEnabled);
        if (usingSsl || port == 465) {
            // prefer TLSv1.2/1.3 for improved interoperability
            props.put("mail.smtp.ssl.protocols", "TLSv1.3 TLSv1.2");
            // ensure we do not attempt STARTTLS when using implicit SSL
            props.put("mail.smtp.starttls.required", "false");
            // don't fallback to non-SSL socket factory if SSL handshake fails
            props.put("mail.smtp.ssl.socketFactory.fallback", "false");
        }

        // Remove legacy socketFactory settings to avoid conflicts with Jakarta Mail implementations
        // (they can cause ClassNotFound on some environments)

        log.info("Mail properties configured: auth={}, starttls={}, ssl={}, debug= {}",
                 smtpAuth, props.getProperty("mail.smtp.starttls.enable"), props.getProperty("mail.smtp.ssl.enable"), mailDebug);
        log.info("JavaMailSender configured successfully");
        return impl;
    }

    // Logging stub implementation to avoid runtime failure when mail not configured
    static class LoggingJavaMailSender implements JavaMailSender {
        private final Logger logger = LoggerFactory.getLogger(LoggingJavaMailSender.class);
        private final Session session = Session.getDefaultInstance(new Properties());

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(session);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            try {
                return new MimeMessage(session, contentStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            try {
                logger.info("[MAIL-STUB] send MimeMessage to: {}", mimeMessage.getAllRecipients());
            } catch (Exception e) {
                logger.info("[MAIL-STUB] send MimeMessage (could not read recipients)");
            }
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            for (MimeMessage m : mimeMessages) send(m);
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            logger.info("[MAIL-STUB] send SimpleMailMessage to={}, subject={}, text={}", (Object) simpleMessage.getTo(), simpleMessage.getSubject(), simpleMessage.getText());
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            for (SimpleMailMessage m : simpleMessages) send(m);
        }
    }
}
