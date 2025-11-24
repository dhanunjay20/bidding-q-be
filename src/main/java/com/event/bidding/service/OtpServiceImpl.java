package com.event.bidding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    private static class OtpEntry {
        String otp;
        Instant expiresAt;
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    private MailService mailService;
    @Autowired
    private WhatsAppService whatsAppService;

    @Override
    public void generateAndSendOtp(String contact) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        OtpEntry entry = new OtpEntry();
        entry.otp = otp;
        entry.expiresAt = Instant.now().plusSeconds(5 * 60); // 5 minutes
        store.put(contact, entry);

        log.info("Generated OTP for contact: {}", contact);
        log.info("OTP value (FOR DEBUGGING ONLY - REMOVE IN PRODUCTION): {}", otp);

        // Send via email or WhatsApp depending on contact format
        if (contact.contains("@")) {
            log.info("Sending OTP via email to: {}", contact);
            mailService.sendSimpleMail(contact, "Your OTP - Event Bidding", "Your OTP is: " + otp + "\n\nThis OTP will expire in 5 minutes.");
            log.info("OTP email sent successfully to: {}", contact);
        } else {
            log.info("Sending OTP via WhatsApp to: {}", contact);
            whatsAppService.sendWhatsAppMessage(contact, "Your OTP for Event Bidding is: " + otp + ". This OTP will expire in 5 minutes.");
            log.info("OTP WhatsApp message sent successfully to: {}", contact);
        }
    }

    @Override
    public boolean verifyOtp(String contact, String otp) {
        log.info("Verifying OTP for contact: {}", contact);
        OtpEntry entry = store.get(contact);
        if (entry == null) {
            log.warn("No OTP found for contact: {}", contact);
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            log.warn("OTP expired for contact: {}", contact);
            store.remove(contact);
            return false;
        }
        boolean ok = entry.otp.equals(otp);
        if (ok) {
            log.info("OTP verified successfully for contact: {}", contact);
            store.remove(contact);
        } else {
            log.warn("Invalid OTP provided for contact: {}", contact);
        }
        return ok;
    }

    @Override
    public void removeOtp(String contact) {
        log.info("Removing OTP for contact: {}", contact);
        store.remove(contact);
    }
}

