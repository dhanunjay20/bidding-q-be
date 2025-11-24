package com.event.bidding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorLoginDto {
    private String login; // can be email or mobile
    private String password;
}
