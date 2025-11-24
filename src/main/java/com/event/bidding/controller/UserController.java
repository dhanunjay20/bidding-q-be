package com.event.bidding.controller;

import com.event.bidding.dto.UserRegistrationDto;
import com.event.bidding.dto.UserUpdateDto;
import com.event.bidding.dto.UserResponseDto;
import com.event.bidding.entity.User;
import com.event.bidding.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private UserResponseDto toDto(User user) {
        if (user == null) return null;
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setAddresses(user.getAddresses());
        dto.setProfileUrl(user.getProfileUrl());
        return dto;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRegistrationDto dto) {
        log.info("User registration request for email: {}", dto.getEmail());
        User user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable String id) {
        log.debug("Get user request for id: {}", id);
        return ResponseEntity.ok(toDto(userService.getUserById(id)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.debug("Get all users request");
        return ResponseEntity.ok(userService.getAllUsers().stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateDto dto) {
        log.info("Update user request for id: {}", id);
        return ResponseEntity.ok(toDto(userService.updateUser(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Delete user request for id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
