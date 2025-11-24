package com.event.bidding.repository;

import com.event.bidding.entity.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Optional<Vendor> findByEmail(String email);
    Optional<Vendor> findByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    boolean existsByVendorOrganizationId(String vendorOrganizationId);
    Optional<Vendor> findByVendorOrganizationId(String vendorOrganizationId);
}
