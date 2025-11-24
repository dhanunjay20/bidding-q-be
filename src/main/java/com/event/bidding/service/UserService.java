package com.event.bidding.service;

import com.event.bidding.dto.UserRegistrationDto;
import com.event.bidding.dto.UserUpdateDto;
import com.event.bidding.entity.User;

import java.util.List;

public interface UserService {
    User createUser(UserRegistrationDto dto);
    User getUserById(String id);
    List<User> getAllUsers();
    User updateUser(String id, UserUpdateDto dto);
    void deleteUser(String id);
}
