package com.event.bidding.service;

import com.event.bidding.dto.*;
import java.util.List;

public interface VendorService {
    VendorResponseDto registerVendor(VendorRegistrationDto dto);
    VendorResponseDto loginVendor(String login, String password);
    VendorResponseDto getVendorById(String id);
    List<VendorResponseDto> getAllVendors();
    // add update/delete as needed for full CRUD
}
