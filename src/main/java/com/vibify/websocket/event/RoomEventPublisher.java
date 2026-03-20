package com.vibify.websocket.event;

import com.vibify.websocket.dto.WsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishPlaybackEvent(UUID roomId, WsEvent<?> event) {

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/playback",
                event
        );
    }

    public void publishQueueEvent(UUID roomId, WsEvent<?> event) {

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/queue",
                event
        );
    }

    public void publishRoomEvent(UUID roomId, WsEvent<?> event) {

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/room",
                event
        );
    }

    public void publishStateEvent(UUID roomId, WsEvent<?> event) {

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/state",
                event
        );
    }

    public void publishRoomEnded(UUID roomId) {

        WsEvent<Void> event = WsEvent.of(
                RoomEventType.ROOM_ENDED,
                roomId,
                null
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/room",
                event
        );
    }

    public void publishMemberLeft(UUID roomId, Long userId) {

        WsEvent<Void> event = WsEvent.<Void>builder()
                .eventType(RoomEventType.ROOM_MEMBER_LEFT)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/room",
                event
        );
    }
}
