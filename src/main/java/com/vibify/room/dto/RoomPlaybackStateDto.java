package com.vibify.room.dto;

import com.vibify.room.model.playback_state.PlaybackStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlaybackStateDto {

    private UUID roomId;

    private Long songId;

    private String title;

    private String artist;

    private String coverImage;

    private Integer duration;

    private String hlsUrl;

    private Long queueItemId;

    private Long offsetMillis;

    private PlaybackStatus status;

    private Long version;

    private Long serverTime;

    private Long lastActionBy;

    private Long startedAt;

    private Long updatedAt;

}