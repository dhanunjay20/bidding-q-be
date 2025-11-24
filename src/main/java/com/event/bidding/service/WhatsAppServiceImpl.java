package com.event.bidding.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppServiceImpl implements WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppServiceImpl.class);

    @Value("${twilio.accountSid:}")
    private String accountSid;

    @Value("${twilio.authToken:}")
    private String authToken;

    @Value("${twilio.apiKeySid:}")
    private String apiKeySid;

    @Value("${twilio.apiKeySecret:}")
    private String apiKeySecret;

    @Value("${twilio.whatsappFrom:}")
    private String whatsappFrom; // e.g. "whatsapp:+14155238886"

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            if (apiKeySid != null && !apiKeySid.isEmpty() && apiKeySecret != null && !apiKeySecret.isEmpty() && accountSid != null && !accountSid.isEmpty()) {
                if (whatsappFrom == null || whatsappFrom.trim().isEmpty()) {
                    log.error("Twilio configured (API Key) but twilio.whatsappFrom is not set; falling back to stub logging.");
                    initialized = false;
                    return;
                }
                // Initialize with API Key (recommended for rotation)
                Twilio.init(apiKeySid, apiKeySecret, accountSid);
                initialized = true;
                log.info("Twilio initialized with API Key successfully");
                return;
            }
            if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
                if (whatsappFrom == null || whatsappFrom.trim().isEmpty()) {
                    log.error("Twilio configured (Account SID) but twilio.whatsappFrom is not set; falling back to stub logging.");
                    initialized = false;
                    return;
                }
                Twilio.init(accountSid, authToken);
                initialized = true;
                log.info("Twilio initialized with Account SID successfully. WhatsApp From: {}", whatsappFrom);
            } else {
                log.warn("Twilio not configured - no credentials found");
            }
        } catch (Exception e) {
            log.error("Twilio init failed: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    @Override
    public void sendWhatsAppMessage(String mobile, String message) {
        log.info("[WhatsApp] Attempting to send message to: {}", mobile);

        if (!initialized) {
            // Twilio not configured — fallback to logging for dev/test
            log.warn("[WhatsApp-STUB] to={} msg={}", mobile, message);
            log.warn("[WhatsApp-STUB] Message logged successfully (Twilio not configured)");
            return;
        }

        // Format the 'to' number - ensure it has whatsapp: prefix and proper country code
        String to;
        if (mobile.startsWith("whatsapp:")) {
            to = mobile;
        } else if (mobile.startsWith("+")) {
            to = "whatsapp:" + mobile;
        } else {
            // Assume it's an Indian number without country code (starts with 9)
            to = "whatsapp:+91" + mobile;
        }

        String from;
        if (whatsappFrom != null && !whatsappFrom.trim().isEmpty()) {
            from = whatsappFrom.startsWith("whatsapp:") ? whatsappFrom : ("whatsapp:" + whatsappFrom);
        } else {
            // This should not happen because we check at init, but guard defensively
            log.error("twilio.whatsappFrom is empty at send time; falling back to stub logging.");
            log.warn("[WhatsApp-STUB] to={} msg={}", mobile, message);
            return;
        }

        try {
            log.info("[WhatsApp] Sending via Twilio from: {} to: {}", from, to);
            log.info("[WhatsApp] Message content: {}", message);
            Message twilioMessage = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(from),
                message
            ).create();
            log.info("[WhatsApp] Message sent successfully. SID: {}", twilioMessage.getSid());
            log.info("[WhatsApp] Message status: {}", twilioMessage.getStatus());
        } catch (ApiException e) {
            log.error("Twilio WhatsApp send failed: {}", e.getMessage());
            log.error("Twilio error code: {}", e.getCode());
            log.error("Twilio error details: {}", e.getMoreInfo());
            log.error("Full stack trace:", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending WhatsApp message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }
}
