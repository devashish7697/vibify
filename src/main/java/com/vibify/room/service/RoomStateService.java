package com.vibify.room.service;

import com.vibify.room.dto.RoomStateResponseDto;
import java.util.UUID;

public interface RoomStateService {

    /**
     * Fetch complete state of the room.
     * Includes:
     *  - room info
     *  - playback state
     *  - queue
     *  - members
     */
    RoomStateResponseDto getRoomState(UUID roomId, Long requestingUserId);

}