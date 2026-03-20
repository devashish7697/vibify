package com.vibify.room.service;

import com.vibify.room.model.room_presence.UserRoomPresence;
import com.vibify.room.repository.UserRoomPresenceRepository;
import com.vibify.websocket.dto.WsEvent;
import com.vibify.websocket.event.RoomEventPublisher;
import com.vibify.websocket.event.RoomEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomPresenceServiceImpl implements RoomPresenceService {

    private final UserRoomPresenceRepository presenceRepository;
    private final RoomEventPublisher eventPublisher;

    @Override
    @Transactional
    public void joinPresence(UUID roomId, Long userId) {

        UserRoomPresence presence =
                presenceRepository.findByRoomIdAndUserId(roomId,userId)
                        .orElse(null);

        if(presence == null){

            presence = UserRoomPresence.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .joinedAt(LocalDateTime.now())
                    .lastPing(LocalDateTime.now())
                    .isActive(true)
                    .build();

        } else {

            presence.setIsActive(true);
            presence.setLastPing(LocalDateTime.now());
        }

        presenceRepository.save(presence);

        publishJoinEvent(roomId,userId);
    }

    @Override
    @Transactional
    public void heartbeat(UUID roomId, Long userId) {

        UserRoomPresence presence =
                presenceRepository.findByRoomIdAndUserId(roomId, userId)
                        .orElse(null);

        if (presence == null) {
            log.warn("Heartbeat without presence → auto joining");
            joinPresence(roomId, userId);
            return;
        }

        presenceRepository.updateHeartbeat(
                roomId,
                userId,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void leavePresence(UUID roomId, Long userId) {

        if (!presenceRepository.existsByRoomIdAndUserId(roomId, userId)) {
            return; // safe exit
        }

        presenceRepository.markInactive(roomId,userId);
        publishLeaveEvent(roomId,userId);
    }

    private void publishJoinEvent(UUID roomId, Long userId){

        WsEvent<Long> event = WsEvent.<Long>builder()
                .eventType(RoomEventType.ROOM_MEMBER_JOINED)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .data(userId)
                .build();

        eventPublisher.publishRoomEvent(roomId,event);
    }

    private void publishLeaveEvent(UUID roomId, Long userId){

        WsEvent<Long> event = WsEvent.<Long>builder()
                .eventType(RoomEventType.ROOM_MEMBER_LEFT)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .data(userId)
                .build();

        eventPublisher.publishRoomEvent(roomId,event);
    }
}