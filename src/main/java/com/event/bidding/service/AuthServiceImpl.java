package com.event.bidding.service;

import com.event.bidding.dto.LoginRequestDto;
import com.event.bidding.dto.AuthResponseDto;
import com.event.bidding.entity.User;
import com.event.bidding.entity.Vendor;
import com.event.bidding.repository.UserRepository;
import com.event.bidding.repository.VendorRepository;
import com.event.bidding.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VendorRepository vendorRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MailService mailService;
    @Autowired
    private WhatsAppService whatsAppService;

    @Override
    public AuthResponseDto login(LoginRequestDto loginDto) {
        User user = userRepo.findByEmail(loginDto.getLogin()).orElse(
                userRepo.findByMobile(loginDto.getLogin()).orElse(null));
        if (user != null && passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            String jwt = jwtUtil.generateToken(user.getEmail(), "USER");
            return AuthResponseDto.ofUser(user, jwt);
        }
        Vendor vendor = vendorRepo.findByEmail(loginDto.getLogin()).orElse(
                vendorRepo.findByMobile(loginDto.getLogin()).orElse(null));
        if (vendor != null && passwordEncoder.matches(loginDto.getPassword(), vendor.getPasswordHash())) {
            String jwt = jwtUtil.generateToken(vendor.getEmail(), "VENDOR");
            return AuthResponseDto.ofVendor(vendor, jwt);
        }
        throw new RuntimeException("Invalid login credentials");
    }

    @Override
    public void sendUsernameToUser(String contact) {
        try {
            User user;
            Vendor vendor = null;

            // Check if contact is email or mobile
            if (contact.contains("@")) {
                user = userRepo.findByEmail(contact).orElse(null);
                if (user == null) {
                    vendor = vendorRepo.findByEmail(contact).orElse(null);
                }
            } else {
                user = userRepo.findByMobile(contact).orElse(null);
                if (user == null) {
                    vendor = vendorRepo.findByMobile(contact).orElse(null);
                }
            }

            if (user != null) {
                String username = user.getEmail(); // Using email as username
                String message = "Your username is: " + username;

                if (contact.contains("@")) {
                    log.info("Sending username to email: {}", contact);
                    mailService.sendSimpleMail(contact, "Your Username - Event Bidding", message);
                } else {
                    log.info("Sending username to WhatsApp: {}", contact);
                    whatsAppService.sendWhatsAppMessage(contact, message);
                }
            } else if (vendor != null) {
                String username = vendor.getEmail(); // Using email as username
                String message = "Your username (Vendor) is: " + username;

                if (contact.contains("@")) {
                    log.info("Sending vendor username to email: {}", contact);
                    mailService.sendSimpleMail(contact, "Your Username - Event Bidding", message);
                } else {
                    log.info("Sending vendor username to WhatsApp: {}", contact);
                    whatsAppService.sendWhatsAppMessage(contact, message);
                }
            } else {
                log.warn("No user or vendor found for contact: {}", contact);
            }
        } catch (Exception e) {
            log.error("Error sending username to contact: {}", contact, e);
            throw new RuntimeException("Failed to send username", e);
        }
    }
}
