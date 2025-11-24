package com.event.bidding.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendors")
public class Vendor {
    @Id
    private String id;
    @Indexed(unique = true)
    private String vendorOrganizationId;
    private String businessName;
    private String contactName;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String mobile;
    private String passwordHash;
    private List<Address> addresses;
    private List<LicenseDocument> licenseDocuments;
}
