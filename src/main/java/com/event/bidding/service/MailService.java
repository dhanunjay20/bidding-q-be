package com.event.bidding.service;

public interface MailService {
    void sendSimpleMail(String to, String subject, String body);
}
