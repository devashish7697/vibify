package com.vibify.playback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaybackResponse {

    private String mode; // ROOM / SOLO
    private Object data; // RoomPlaybackStateDto or future Solo DTO
}
