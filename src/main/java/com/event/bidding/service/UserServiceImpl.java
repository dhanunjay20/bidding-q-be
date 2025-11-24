package com.event.bidding.service;

import com.event.bidding.dto.UserRegistrationDto;
import com.event.bidding.dto.UserUpdateDto;
import com.event.bidding.entity.User;
import com.event.bidding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserRegistrationDto dto) {
        if (userRepo.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already registered");
        if (userRepo.existsByMobile(dto.getMobile()))
            throw new RuntimeException("Mobile number already registered");
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setAddresses(dto.getAddresses());
        user.setProfileUrl(dto.getProfileUrl());
        return userRepo.save(user);
    }

    @Override
    public User getUserById(String id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User updateUser(String id, UserUpdateDto dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Update fields if provided (allow full overwrite when present)
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getMobile() != null) user.setMobile(dto.getMobile());

        // Replace addresses list if provided
        if (dto.getAddresses() != null) {
            user.setAddresses(dto.getAddresses());
        }
        // Or add a single address if provided
        if (dto.getAddress() != null) {
            if (user.getAddresses() == null) user.setAddresses(new java.util.ArrayList<>());
            user.getAddresses().add(dto.getAddress());
        }

        // Update profile URL if provided
        if (dto.getProfileUrl() != null) {
            user.setProfileUrl(dto.getProfileUrl());
        }

        // Update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepo.save(user);
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepo.existsById(id))
            throw new RuntimeException("User not found");
        userRepo.deleteById(id);
    }
}
