package com.vibify.notification.service;

public interface UserDeviceService {

    void registerDevice(Long userId, String fcmToken, String platform);

}
