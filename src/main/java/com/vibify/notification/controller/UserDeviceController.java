package com.vibify.notification.controller;

import com.vibify.auth.security.UserPrincipal;
import com.vibify.common.globalResponse.GlobalApiResponse;
import com.vibify.notification.dto.DeviceRegistrationRequest;
import com.vibify.notification.service.UserDeviceService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping("/register")
    public GlobalApiResponse registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        Long userId = userPrincipal.getId();

        userDeviceService.registerDevice(
                userId,
                request.getFcmToken(),
                request.getPlatform()
        );

        return GlobalApiResponse.success("Device registered successfully");
    }
}
