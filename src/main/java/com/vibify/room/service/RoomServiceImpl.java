package com.vibify.room.service;

import com.vibify.common.util.InviteCodeGenerator;
import com.vibify.room.dto.CreateRoomRequestDto;
import com.vibify.room.dto.RoomResponseDto;
import com.vibify.common.exception.room_exception.*;
import com.vibify.room.model.playback_state.PlaybackSourceType;
import com.vibify.room.model.playback_state.PlaybackStatus;
import com.vibify.room.model.room_entity.Room;
import com.vibify.room.model.room_member.RoomMember;
import com.vibify.room.model.room_member.RoomMemberRole;
import com.vibify.room.model.playback_state.RoomPlaybackState;
import com.vibify.room.model.room_entity.RoomStatus;
import com.vibify.room.repository.RoomMemberRepository;
import com.vibify.room.repository.RoomPlaybackStateRepository;
import com.vibify.room.repository.RoomRepository;
import com.vibify.room.service.RoomService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomPlaybackStateRepository playbackStateRepository;

    /**
     * Create a new room.
     * Steps:
     * 1. Generate unique invite code
     * 2. Persist room
     * 3. Add host as first member
     * 4. Initialize playback state
     */
    @Override
    @Transactional
    public RoomResponseDto createRoom(CreateRoomRequestDto request, Long hostUserId) {

        // Generate unique invite code
        String inviteCode = generateUniqueInviteCode();

        // Create room entity
        Room room = Room.builder()
                .name(request.getName())
                .hostUserId(hostUserId)
                .inviteCode(inviteCode)
                .isPrivate(request.isPrivate())
                .status(RoomStatus.ACTIVE)
                .build();

        Room savedRoom = roomRepository.save(room);

        // Add host as first member
        RoomMember hostMember = RoomMember.builder()
                .roomId(savedRoom.getId())
                .userId(hostUserId)
                .role(RoomMemberRole.HOST)
                .build();

        roomMemberRepository.save(hostMember);

        // Initialize playback state
        RoomPlaybackState playbackState = RoomPlaybackState.builder()
                .roomId(savedRoom.getId())
                .songId(null)
                .queueItemId(null)
                .sourceType(PlaybackSourceType.QUEUE)
                .sourceId(null)
                .startedAt(null)
                .offsetMillis(0L)
                .status(PlaybackStatus.STOPPED)
                .lastActionBy(hostUserId)
                .updatedAt(System.currentTimeMillis())
                .build();

        playbackStateRepository.save(playbackState);

        return mapToRoomResponseDto(savedRoom);
    }

    /**
     * Delete room (host only).
     * Soft delete using RoomStatus.
     */
    @Override
    @Transactional
    public void deleteRoom(UUID roomId, Long requestingUserId) {

        Room room = getRoomEntity(roomId);

        if (!room.getHostUserId().equals(requestingUserId)) {
            throw new RoomAccessDeniedException("Only host can delete the room");
        }

        room.setStatus(RoomStatus.ENDED);
        roomRepository.save(room);
    }

    /**
     * Fetch room by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(UUID roomId) {

        Room room = getRoomEntity(roomId);
        return mapToRoomResponseDto(room);
    }

    /**
     * Fetch room using invite code.
     */
    @Override
    @Transactional(readOnly = true)
    public RoomResponseDto getRoomByInviteCode(String inviteCode) {

        Room room = roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() ->
                        new RoomNotFoundException("Room not found for invite code: " + inviteCode));

        return mapToRoomResponseDto(room);
    }

    /**
     * Internal method used by other services.
     */
    @Override
    @Transactional(readOnly = true)
    public Room getRoomEntity(UUID roomId) {

        return roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RoomNotFoundException("Room not found with id: " + roomId));
    }

    /**
     * Generate a collision-safe invite code.
     */
    private String generateUniqueInviteCode() {

        String inviteCode;

        do {
            inviteCode = InviteCodeGenerator.generateCode();
        } while (roomRepository.existsByInviteCode(inviteCode));

        return inviteCode;
    }

    /**
     * Map Room entity to DTO.
     */
    private RoomResponseDto mapToRoomResponseDto(Room room) {

        return RoomResponseDto.builder()
                .roomId(room.getId())
                .name(room.getName())
                .inviteCode(room.getInviteCode())
                .hostUserId(room.getHostUserId())
                .isPrivate(room.isPrivate())
                .status(room.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomByInviteCodeEntity(String inviteCode) {

        return roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() ->
                        new RoomNotFoundException("Room not found for invite code: " + inviteCode));
    }

    @Override
    @Transactional
    public void updateRoomActivity(UUID roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setLastActivityAt(LocalDateTime.now());
        room.setStatus(RoomStatus.ACTIVE);
    }
}