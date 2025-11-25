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

        // Increase timeouts to allow for network latency but still fail reasonably
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");

        // Decide SSL/STARTTLS behaviour
        boolean sslExplicit = sslEnable != null && !sslEnable.isEmpty();
        boolean usingSsl = false;
        if (!sslExplicit) {
            if (port == 465) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "false");
                usingSsl = true;
            } else if (port == 587) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.ssl.enable", "false");
            } else {
                props.put("mail.smtp.starttls.enable", starttlsEnable != null ? starttlsEnable : "true");
            }
        } else {
            // user provided explicit setting
            props.put("mail.smtp.ssl.enable", sslEnable);
            props.put("mail.smtp.starttls.enable", starttlsEnable != null ? starttlsEnable : "true");
            usingSsl = Boolean.parseBoolean(sslEnable);
            // If port 465 -> implicit SSL is expected; ensure we use SSL
            if (port == 465 && !usingSsl) {
                log.warn("Port 465 detected but mail.smtp.ssl.enable explicitly set to false — overriding to true for port 465");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "false");
                usingSsl = true;
            }
        }

        // For implicit SSL (port 465) prefer the smtps protocol, and add socket factory hints for compatibility
        if (usingSsl || port == 465) {
            impl.setProtocol("smtps");
            props.put("mail.smtp.ssl.protocols", "TLSv1.3 TLSv1.2");
            props.put("mail.smtp.starttls.required", "false");
            props.put("mail.smtp.ssl.socketFactory.fallback", "false");
            // Socket factory class (legacy but increases compatibility with some providers)
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            if (port > 0) props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            // Be explicit about trust to avoid certificate issues during handshake
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.ssl.checkserveridentity", "true");
        } else {
            // For non-SSL connections ensure STARTTLS behaviour is as configured
            props.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable", "true"));
        }

        // Always log the resolved mail properties (masked sensitive values)
        String sslVal = props.getProperty("mail.smtp.ssl.enable");
        String starttlsVal = props.getProperty("mail.smtp.starttls.enable");
        log.info("Mail properties configured: auth={}, starttls={}, ssl={}, debug={}", smtpAuth, starttlsVal, sslVal, mailDebug);
        log.info("JavaMailSender configured successfully (protocol={})", impl.getProtocol());
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
