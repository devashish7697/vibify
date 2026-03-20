package com.vibify.notification.service;

import com.vibify.notification.model.UserDevice;
import com.vibify.notification.repository.UserDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserDeviceServiceImpl implements UserDeviceService {

    private static final Logger logger = LoggerFactory.getLogger(UserDeviceServiceImpl.class);

    private final UserDeviceRepository repository;

    public UserDeviceServiceImpl(UserDeviceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void registerDevice(Long userId, String fcmToken, String platform) {

        // 🔹 Normalize platform
        String normalizedPlatform = normalizePlatform(platform);

        // 🔹 Find existing device by token
        Optional<UserDevice> existingOpt = repository.findByFcmToken(fcmToken);

        if (existingOpt.isPresent()) {

            UserDevice existing = existingOpt.get();

            // 🔥 Update only if changed (avoid unnecessary DB writes)
            boolean needsUpdate = false;

            if (!existing.getUserId().equals(userId)) {
                existing.setUserId(userId);
                needsUpdate = true;
            }

            if (!existing.getPlatform().equals(normalizedPlatform)) {
                existing.setPlatform(normalizedPlatform);
                needsUpdate = true;
            }

            if (needsUpdate) {
                repository.save(existing);
                logger.info("Updated existing device for token={}", fcmToken);
            } else {
                logger.debug("Device already up-to-date for token={}", fcmToken);
            }

        } else {

            // 🔹 Create new device
            UserDevice device = UserDevice.builder()
                    .userId(userId)
                    .fcmToken(fcmToken)
                    .platform(normalizedPlatform)
                    .build();

            repository.save(device);

            logger.info("Registered new device for userId={}", userId);
        }
    }

    // ---------------- PRIVATE METHODS ----------------

    private String normalizePlatform(String platform) {

        String normalized = platform.trim().toUpperCase(Locale.ROOT);

        if (!normalized.equals("ANDROID") && !normalized.equals("IOS")) {
            throw new IllegalArgumentException("Invalid platform: " + platform);
        }

        return normalized;
    }
}
