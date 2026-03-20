package com.vibify.notification.service;

import com.vibify.common.exception.InvalidFcmTokenException;
import com.vibify.notification.client.PushClient;
import com.vibify.notification.model.UserDevice;
import com.vibify.notification.repository.UserDeviceRepository;
import com.vibify.room.model.playback_state.RoomPlaybackState;
import com.vibify.room.model.room_member.RoomMember;
import com.vibify.room.repository.RoomMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final UserDeviceRepository userDeviceRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final PushClient pushClient;

    public NotificationServiceImpl(
            UserDeviceRepository userDeviceRepository,
            RoomMemberRepository roomMemberRepository,
            PushClient pushClient
    ) {
        this.userDeviceRepository = userDeviceRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.pushClient = pushClient;
    }

    @Override
    public void sendPlaybackUpdate(RoomPlaybackState state, Long triggeredByUserId) {

        try {

            UUID roomId = state.getRoomId();

            // 🔹 Step 1: Fetch room members
            List<RoomMember> members = roomMemberRepository.findByRoomIdAndLeftAtIsNull(roomId);

            if (members.isEmpty()) {
                logger.warn("No members found for roomId={}", roomId);
                return;
            }

            // 🔹 Step 2: Extract userIds
            Set<Long> userIds = members.stream()
                    .map(RoomMember::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 🔹 Step 3: Remove sender
            userIds.remove(triggeredByUserId);

            if (userIds.isEmpty()) {
                logger.debug("No target users for notification in roomId={}", roomId);
                return;
            }

            // 🔹 Step 4: Fetch devices
            List<UserDevice> devices = userDeviceRepository.findByUserIdIn(userIds);

            if (devices.isEmpty()) {
                logger.debug("No devices found for users in roomId={}", roomId);
                return;
            }

            // 🔹 Step 5: Build payload
            Map<String, Object> payload = buildPayload(state, triggeredByUserId);

            // 🔹 Step 6: Send push
            for (UserDevice device : devices) {

                try {
                    pushClient.send(device.getFcmToken(), payload);
                } catch (InvalidFcmTokenException ex) {

                    logger.warn("Removing invalid FCM token={}", device.getFcmToken());

                    try {
                        userDeviceRepository.deleteByFcmToken(device.getFcmToken());
                    } catch (Exception deleteEx) {
                        logger.error("Failed to delete invalid token={}", device.getFcmToken(), deleteEx);
                    }

                } catch (Exception ex) {
                    logger.error("Failed to send push to token={}", device.getFcmToken(), ex);

                    // 🔥 FUTURE: handle invalid token cleanup here
                }
            }

            logger.info("Playback push sent for roomId={}, recipients={}", roomId, devices.size());

        }  catch (Exception e) {
            // 🔴 NEVER break playback flow
            logger.error("Error in sendPlaybackUpdate", e);
        }
    }

    // ---------------- PRIVATE METHODS ----------------

    private Map<String, Object> buildPayload(RoomPlaybackState state, Long triggeredByUserId) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("type", "PLAYBACK_UPDATE");
        payload.put("roomId", state.getRoomId());

        // ✅ Null-safe handling
        payload.put("songId", state.getSongId() != null ? state.getSongId() : -1);
        payload.put("status", state.getStatus() != null ? state.getStatus().name() : "STOPPED");

        payload.put("startedAt", state.getStartedAt() != null ? state.getStartedAt() : 0L);
        payload.put("offsetMillis", state.getOffsetMillis() != null ? state.getOffsetMillis() : 0L);

        payload.put("stateVersion", state.getStateVersion());
        payload.put("updatedAt", state.getUpdatedAt());
        payload.put("triggeredBy", triggeredByUserId);

        return payload;
    }
}
