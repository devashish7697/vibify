package com.vibify.notification.repository;

import com.vibify.notification.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    /**
     * Find device by FCM token (used for upsert logic)
     */
    Optional<UserDevice> findByFcmToken(String fcmToken);

    /**
     * Fetch all devices for a single user
     */
    List<UserDevice> findByUserId(Long userId);

    /**
     * Fetch devices for multiple users (CORE for notification fan-out)
     */
    List<UserDevice> findByUserIdIn(Collection<Long> userIds);

    /**
     * Delete device by token (used for invalid token cleanup)
     */
    @Modifying
    @Transactional
    void deleteByFcmToken(String fcmToken);
}