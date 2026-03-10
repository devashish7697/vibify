package com.vibify.auth.controller;

import com.vibify.auth.dto.*;
import com.vibify.auth.service.AuthService;
import com.vibify.common.globalResponse.GlobalApiResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public GlobalApiResponse<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        AuthResponse response = authService.register(request);

        return GlobalApiResponse.success(
                "User registered successfully",
                response
        );
    }

    @PostMapping("/login")
    public GlobalApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResponse response = authService.login(request);

        return GlobalApiResponse.success(
                "Login successful",
                response
        );
    }
}