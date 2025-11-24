package com.event.bidding.service.impl;

import com.event.bidding.dto.*;
import com.event.bidding.entity.Vendor;
import com.event.bidding.repository.VendorRepository;
import com.event.bidding.service.VendorService;
import com.event.bidding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorRepository vendorRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public VendorResponseDto registerVendor(VendorRegistrationDto dto) {
        if (vendorRepo.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already registered");
        if (vendorRepo.existsByMobile(dto.getMobile()))
            throw new RuntimeException("Mobile number already registered");
        if (vendorRepo.existsByVendorOrganizationId(dto.getVendorOrganizationId()))
            throw new RuntimeException("Organization ID already registered");

        Vendor vendor = new Vendor();
        vendor.setVendorOrganizationId(dto.getVendorOrganizationId());
        vendor.setBusinessName(dto.getBusinessName());
        vendor.setContactName(dto.getContactName());
        vendor.setEmail(dto.getEmail());
        vendor.setMobile(dto.getMobile());
        vendor.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        vendor.setAddresses(dto.getAddresses());
        vendor.setLicenseDocuments(dto.getLicenseDocuments());
        Vendor saved = vendorRepo.save(vendor);
        return toResponseDto(saved);
    }

    @Override
    public VendorResponseDto loginVendor(String login, String password) {
        Vendor vendor = vendorRepo.findByEmail(login)
                .or(() -> vendorRepo.findByMobile(login))
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (!passwordEncoder.matches(password, vendor.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");
        // Optionally: generate JWT and add to response DTO
        return toResponseDto(vendor);
    }

    @Override
    public VendorResponseDto getVendorById(String id) {
        return vendorRepo.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
    }

    @Override
    public List<VendorResponseDto> getAllVendors() {
        return vendorRepo.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private VendorResponseDto toResponseDto(Vendor v) {
        return new VendorResponseDto(
                v.getId(),
                v.getVendorOrganizationId(),
                v.getBusinessName(),
                v.getContactName(),
                v.getEmail(),
                v.getMobile(),
                v.getAddresses(),
                v.getLicenseDocuments()
        );
    }
}
