package com.event.bidding.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseDocument {
    private String country;
    private String formatType;
    private String licenseNumber;
    private String issuingAuthority;
    private String validFrom;
    private String validTill;
    private String docFileUrl;
}
