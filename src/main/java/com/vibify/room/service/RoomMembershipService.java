package com.vibify.room.service;

import com.vibify.room.dto.RoomMemberDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomMembershipService {

    /**
     * Join a room using invite code.
     */
    void joinRoom(String inviteCode, Long userId);

    /**
     * Leave a room.
     */
    void leaveRoom(UUID roomId, Long userId);

    /**
     * Get all active members of a room.
     */
    List<RoomMemberDto> getRoomMembers(UUID roomId);

    /**
     * Validate that a user is part of the room.
     * Throws RoomNotMemberException if not.
     */
    void validateUserMembership(UUID roomId, Long userId);

    /**
     * Check if user is the host of the room.
     */
    boolean isUserHost(UUID roomId, Long userId);

    /**
     * Check if a user is associated with any room or not, so we can decide playback mode
     */

    Optional<UUID> getActiveRoomId(Long userId);

    UUID getActiveRoomIdOrThrow(Long userId);

    boolean isUserInAnyRoom(Long userId);

}
