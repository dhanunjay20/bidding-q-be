package com.event.bidding.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "Login (email or mobile) is required")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;
}
