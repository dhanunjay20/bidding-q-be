package com.event.bidding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordDto {
    @Email(message = "Email must be valid")
    private String email; // optional

    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile number must be valid")
    private String mobile; // optional
}
