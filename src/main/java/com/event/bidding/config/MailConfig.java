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

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private String sslEnable;

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

        Properties props = impl.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.ssl.enable", sslEnable);
        props.put("mail.debug", mailDebug);

        // Additional properties for better compatibility
        if ("true".equals(sslEnable)) {
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        log.info("Mail properties configured: auth={}, starttls={}, ssl={}, debug={}",
                 smtpAuth, starttlsEnable, sslEnable, mailDebug);
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

