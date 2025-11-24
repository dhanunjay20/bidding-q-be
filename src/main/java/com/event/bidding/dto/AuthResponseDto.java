package com.event.bidding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.event.bidding.entity.User;
import com.event.bidding.entity.Vendor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String userType;
    private String userId;
    private String vendorId;
    private String vendorOrganizationId;
    private String id; // convenience id field
    private String profileUrl; // include profile URL for users

    public static AuthResponseDto ofUser(User user, String token) {
        AuthResponseDto dto = new AuthResponseDto();
        dto.setToken(token);
        dto.setUserType("USER");
        dto.setUserId(user.getId());
        dto.setId(user.getId());
        dto.setProfileUrl(user.getProfileUrl());
        return dto;
    }

    public static AuthResponseDto ofVendor(Vendor vendor, String token) {
        AuthResponseDto dto = new AuthResponseDto();
        dto.setToken(token);
        dto.setUserType("VENDOR");
        dto.setVendorId(vendor.getId());
        dto.setVendorOrganizationId(vendor.getVendorOrganizationId());
        return dto;
    }
}
