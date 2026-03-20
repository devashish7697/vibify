package com.vibify.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRegistrationRequest {

    @NotBlank(message = "FCM token must not be blank")
    @Size(max = 512, message = "FCM token length must be <= 512 characters")
    private String fcmToken;

    @NotBlank(message = "Platform must not be blank")
    private String platform;
}
