package com.event.bidding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.event.bidding.entity.Address;
import com.event.bidding.entity.LicenseDocument;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponseDto {
    private String id;
    private String vendorOrganizationId;
    private String businessName;
    private String contactName;
    private String email;
    private String mobile;
    private List<Address> addresses;
    private List<LicenseDocument> licenseDocuments;
}
