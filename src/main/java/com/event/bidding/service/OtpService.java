package com.event.bidding.service;

public interface OtpService {
    void generateAndSendOtp(String contact);
    boolean verifyOtp(String contact, String otp);
    void removeOtp(String contact);
}
