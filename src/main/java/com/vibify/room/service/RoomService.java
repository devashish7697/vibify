package com.vibify.room.service;

import com.vibify.room.dto.CreateRoomRequestDto;
import com.vibify.room.dto.RoomResponseDto;
import com.vibify.room.model.room_entity.Room;

import java.util.UUID;

public interface RoomService {

    /**
     * Create a new room.
     * The creator becomes the HOST automatically.
     */
    RoomResponseDto createRoom(CreateRoomRequestDto request, Long hostUserId);

    /**
     * Delete a room.
     * Only the HOST is allowed to delete the room.
     */
    void deleteRoom(UUID roomId, Long requestingUserId);

    /**
     * Fetch room details by roomId.
     */
    RoomResponseDto getRoomById(UUID roomId);

    /**
     * Fetch room details using invite code.
     */
    RoomResponseDto getRoomByInviteCode(String inviteCode);

    /**
     * Internal helper used by other services.
     * Returns the Room entity or throws RoomNotFoundException.
     */
    Room getRoomEntity(UUID roomId);

    Room getRoomByInviteCodeEntity(String inviteCode);

}