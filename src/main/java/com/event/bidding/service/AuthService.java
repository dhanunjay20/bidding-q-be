package com.event.bidding.service;

import com.event.bidding.dto.LoginRequestDto;
import com.event.bidding.dto.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto loginDto);
    void sendUsernameToUser(String contact);
}
