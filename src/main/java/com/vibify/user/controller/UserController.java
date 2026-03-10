package com.vibify.user.controller;

import com.vibify.common.globalResponse.GlobalApiResponse;
import com.vibify.user.dto.UpdateProfileRequest;
import com.vibify.user.dto.UserResponse;
import com.vibify.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalApiResponse<UserResponse>> getUserById(
            @PathVariable Long id
    ) {

        UserResponse user = userService.findById(id);

        return ResponseEntity.ok(
                GlobalApiResponse.success("User fetched successfully", user)
        );
    }


    @GetMapping("/username/{username}")
    public ResponseEntity<GlobalApiResponse<UserResponse>> getUserByUsername(
            @PathVariable String username
    ) {

        UserResponse user = userService.findByUsername(username);

        return ResponseEntity.ok(
                GlobalApiResponse.success("User fetched successfully", user)
        );
    }


    @GetMapping("/check-username")
    public ResponseEntity<GlobalApiResponse<Boolean>> checkUsernameAvailability(
            @RequestParam String username
    ) {

        boolean exists = userService.usernameExists(username);

        return ResponseEntity.ok(
                GlobalApiResponse.success(
                        exists ? "Username already taken" : "Username available",
                        !exists
                )
        );
    }


    @GetMapping("/check-email")
    public ResponseEntity<GlobalApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam String email
    ) {

        boolean exists = userService.emailExists(email);

        return ResponseEntity.ok(
                GlobalApiResponse.success(
                        exists ? "Email already registered" : "Email available",
                        !exists
                )
        );
    }


    @PutMapping("/{userId}/profile")
    public ResponseEntity<GlobalApiResponse<UserResponse>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        UserResponse updatedUser = userService.updateProfile(userId, request);

        return ResponseEntity.ok(
                GlobalApiResponse.success("Profile updated successfully", updatedUser)
        );
    }
}
