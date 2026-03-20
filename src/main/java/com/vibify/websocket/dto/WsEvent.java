package com.vibify.websocket.dto;

import com.vibify.websocket.event.RoomEventType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WsEvent<T> {

    private RoomEventType eventType;

    private UUID roomId;

    private Long triggeredBy;

    private long timestamp;

    private Long version;

    private T data;


    public static <T> WsEvent<T> of(RoomEventType type, UUID roomId, T data) {
        return WsEvent.<T>builder()
                .eventType(type)
                .roomId(roomId)
                .timestamp(System.currentTimeMillis())
                .data(data)
                .build();
    }

}