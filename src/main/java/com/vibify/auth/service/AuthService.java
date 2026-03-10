package com.vibify.auth.service;

import com.vibify.auth.dto.*;

public interface AuthService {

    AuthResponse register(SignupRequest request);
    AuthResponse login(LoginRequest request);

}