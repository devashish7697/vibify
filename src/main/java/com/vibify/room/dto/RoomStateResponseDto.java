package com.vibify.room.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStateResponseDto {

    private RoomResponseDto room;

    private RoomPlaybackStateDto playback;

    private List<RoomQueueItemDto> queue;

    private List<RoomMemberDto> members;

    private boolean canManageRoom;

}
