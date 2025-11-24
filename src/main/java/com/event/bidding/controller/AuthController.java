package com.event.bidding.controller;

import com.event.bidding.dto.LoginRequestDto;
import com.event.bidding.dto.AuthResponseDto;
import com.event.bidding.service.AuthService;
import com.event.bidding.dto.ForgotPasswordDto;
import com.event.bidding.dto.ForgotUsernameDto;
import com.event.bidding.dto.VerifyOtpDto;
import com.event.bidding.dto.ResetPasswordDto;
import com.event.bidding.entity.User;
import com.event.bidding.repository.UserRepository;
import com.event.bidding.service.OtpService;
import com.event.bidding.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDto dto) {
        String email = (dto != null) ? dto.getEmail() : null;
        String mobile = (dto != null) ? dto.getMobile() : null;

        try {
            if (email != null && !email.isBlank()) {
                var userOpt = userRepository.findByEmail(email);
                userOpt.ifPresent(u -> otpService.generateAndSendOtp(email));
            } else if (mobile != null && !mobile.isBlank()) {
                var userOpt = userRepository.findByMobile(mobile);
                userOpt.ifPresent(u -> otpService.generateAndSendOtp(mobile));
            }
            // generic response
            return ResponseEntity.ok("If an account exists for the provided contact, an OTP has been sent to reset the password.");
        } catch (Exception e) {
            log.error("Error in forgotPassword for email={} mobile={}", email, mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<String> forgotUsername(@RequestBody ForgotUsernameDto dto) {
        String email = (dto != null) ? dto.getEmail() : null;
        String mobile = (dto != null) ? dto.getMobile() : null;

        try {
            log.info("Forgot username request received for email={}, mobile={}", email, mobile);

            if (email != null && !email.isBlank()) {
                var userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    log.info("User found with email: {}, sending username", email);
                    // Send username directly to registered contact (email)
                    authService.sendUsernameToUser(email);
                } else {
                    log.info("No user found with email: {}", email);
                }
            } else if (mobile != null && !mobile.isBlank()) {
                var userOpt = userRepository.findByMobile(mobile);
                if (userOpt.isPresent()) {
                    log.info("User found with mobile: {}, sending username", mobile);
                    // Send username directly to registered contact (WhatsApp)
                    authService.sendUsernameToUser(mobile);
                } else {
                    log.info("No user found with mobile: {}", mobile);
                }
            } else {
                log.warn("Forgot username request with no email or mobile");
                return ResponseEntity.badRequest().body("Email or mobile number is required");
            }

            // Always return generic response for security
            return ResponseEntity.ok("If an account exists for the provided contact, an email or WhatsApp message has been sent with your username.");
        } catch (Exception e) {
            log.error("Error in forgotUsername for email={} mobile={}", email, mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    @PostMapping("/retrieve-username")
    public ResponseEntity<String> retrieveUsername(@RequestBody @Valid VerifyOtpDto dto) {
        try {
            log.info("Retrieve username request for contact: {}", dto.getContact());

            boolean otpValid = otpService.verifyOtp(dto.getContact(), dto.getOtp());
            if (!otpValid) {
                log.warn("Invalid or expired OTP for contact: {}", dto.getContact());
                return ResponseEntity.badRequest().body("Invalid or expired OTP");
            }

            log.info("OTP verified successfully, sending username to: {}", dto.getContact());
            authService.sendUsernameToUser(dto.getContact());

            return ResponseEntity.ok("Your username has been sent to your registered contact.");
        } catch (Exception e) {
            log.error("Error in retrieveUsername for contact={}", dto.getContact(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to retrieve username");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid VerifyOtpDto dto) {
        boolean ok = otpService.verifyOtp(dto.getContact(), dto.getOtp());
        if (!ok) return ResponseEntity.badRequest().body("Invalid or expired OTP");
        return ResponseEntity.ok("OTP verified");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordDto dto) {
        boolean ok = otpService.verifyOtp(dto.getContact(), dto.getOtp());
        if (!ok) return ResponseEntity.badRequest().body("Invalid or expired OTP");
        // find user and update password
        User user = null;
        if (dto.getContact() != null && dto.getContact().contains("@")) {
            user = userRepository.findByEmail(dto.getContact()).orElse(null);
        } else if (dto.getContact() != null) {
            user = userRepository.findByMobile(dto.getContact()).orElse(null);
        }
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        // reuse UserService updateUser to update password
        com.event.bidding.dto.UserUpdateDto upd = new com.event.bidding.dto.UserUpdateDto();
        upd.setPassword(dto.getNewPassword());
        userService.updateUser(user.getId(), upd);
        return ResponseEntity.ok("Password reset successful");
    }
}
