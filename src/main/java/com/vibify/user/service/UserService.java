package com.vibify.user.service;

import com.vibify.user.dto.CreateUserRequest;
import com.vibify.user.dto.UpdateProfileRequest;
import com.vibify.user.dto.UserResponse;

import java.util.Optional;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    void updateLastSeen(Long userId);

    void updateOnlineStatus(Long userId, boolean online);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

}