package com.vibify.room.service;

import com.vibify.room.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomStateServiceImpl implements RoomStateService {

    private final RoomService roomService;
    private final RoomMembershipService roomMembershipService;
    private final RoomQueueService roomQueueService;
    private final RoomPlaybackService roomPlaybackService;

    /**
     * Fetch full room state for UI
     */
    @Override
    @Transactional(readOnly = true)
    public RoomStateResponseDto getRoomState(UUID roomId, Long userId) {

        // 1️⃣ validate user belongs to room
        roomMembershipService.validateUserMembership(roomId, userId);

        // 2️⃣ fetch room info
        RoomResponseDto room = roomService.getRoomById(roomId);

        // 3️⃣ fetch members
        List<RoomMemberDto> members = roomMembershipService.getRoomMembers(roomId);

        // 4️⃣ fetch queue
        List<RoomQueueItemDto> queue = roomQueueService.getRoomQueue(roomId);

        // 5️⃣ fetch playback
        RoomPlaybackStateDto playback = roomPlaybackService.getPlaybackState(roomId);

        // 6️⃣ determine permissions
        boolean canManageRoom =
                roomMembershipService.isUserHost(roomId, userId);

        return RoomStateResponseDto.builder()
                .room(room)
                .members(members)
                .queue(queue)
                .playback(playback)
                .canManageRoom(canManageRoom)
                .build();
    }
}
