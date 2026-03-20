package com.vibify.room.service;

import com.vibify.room.dto.RoomMemberDto;
import com.vibify.common.exception.room_exception.*;
import com.vibify.room.model.room_entity.Room;
import com.vibify.room.model.room_member.RoomMember;
import com.vibify.room.model.room_member.RoomMemberRole;
import com.vibify.room.repository.RoomMemberRepository;
import com.vibify.room.repository.RoomRepository;
import com.vibify.room.repository.UserRoomPresenceRepository;
import com.vibify.room.service.RoomMembershipService;
import com.vibify.room.service.RoomService;

import com.vibify.user.model.User;
import com.vibify.user.repository.UserRepository;
import com.vibify.websocket.dto.WsEvent;
import com.vibify.websocket.event.RoomEventPublisher;
import com.vibify.websocket.event.RoomEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomMembershipServiceImpl implements RoomMembershipService {

    private final RoomMemberRepository roomMemberRepository;
    private final RoomService roomService;
    private final UserRepository userRepository;
    private final UserRoomPresenceRepository presenceRepository;

    private final RoomEventPublisher eventPublisher;

    /**
     * Join room using invite code.
     */
    @Override
    @Transactional
    public void joinRoom(String inviteCode, Long userId) {

        Room room = roomService.getRoomByInviteCodeEntity(inviteCode);
        handleMembershipJoin(room.getId(), userId);
    }

    /**
     * Leave room.
     */
    @Override
    @Transactional
    public void leaveRoom(UUID roomId, Long userId) {

        Room room = roomService.getRoomEntity(roomId);

        if (room.getHostUserId().equals(userId)) {
            throw new RoomAccessDeniedException("Host cannot leave the room. Delete the room instead.");
        }

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)
                .orElseThrow(() -> new RoomNotMemberException("User not part of room"));

        member.setLeftAt(LocalDateTime.now());

        roomMemberRepository.save(member);
        publishMemberLeft(roomId, userId);
    }

    /**
     * Fetch active members.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomMemberDto> getRoomMembers(UUID roomId) {

        return roomMemberRepository
                .findByRoomIdAndLeftAtIsNull(roomId)
                .stream()
                .map(member -> {

                    User user = userRepository.findById(member.getUserId())
                            .orElse(null);

                    boolean isOnline = presenceRepository
                            .existsByRoomIdAndUserIdAndIsActiveTrue(roomId, member.getUserId());


                    return RoomMemberDto.builder()
                            .userId(member.getUserId())
                            .username(user != null ? user.getUsername() : null)
                            .profileImage(user != null ? user.getProfileImage() : null)
                            .role(member.getRole().name())
                            .isOnline(isOnline)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Validate user membership.
     */
    @Override
    @Transactional(readOnly = true)
    public void validateUserMembership(UUID roomId, Long userId) {

        boolean exists = roomMemberRepository
                .existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId);

        if (!exists) {
            throw new RoomNotMemberException("User not a member of this room");
        }
    }

    /**
     * Check if user is host.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isUserHost(UUID roomId, Long userId) {

        Room room = roomService.getRoomEntity(roomId);

        return room.getHostUserId().equals(userId);
    }

    /**
     * Handles membership join logic including rejoin.
     */
    private void handleMembershipJoin(UUID roomId, Long userId) {

        RoomMember existingMember = roomMemberRepository
                .findByRoomIdAndUserId(roomId, userId)
                .orElse(null);

        if (existingMember == null) {

            RoomMember newMember = RoomMember.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .role(RoomMemberRole.MEMBER)
                    .build();

            roomMemberRepository.save(newMember);
            publishMemberJoined(roomId, userId);
            return;
        }

        if (existingMember.getLeftAt() == null) {
            throw new RoomAlreadyJoinedException("User already joined this room");
        }

        existingMember.setLeftAt(null);
        existingMember.setJoinedAt(LocalDateTime.now());

        roomMemberRepository.save(existingMember);
        publishMemberJoined(roomId, userId);
    }

    private void publishMemberJoined(UUID roomId, Long userId) {

        WsEvent<Long> event = WsEvent.<Long>builder()
                .eventType(RoomEventType.ROOM_MEMBER_JOINED)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .data(userId)
                .build();

        eventPublisher.publishRoomEvent(roomId, event);
    }

    private void publishMemberLeft(UUID roomId, Long userId) {

        WsEvent<Long> event = WsEvent.<Long>builder()
                .eventType(RoomEventType.ROOM_MEMBER_LEFT)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .data(userId)
                .build();

        eventPublisher.publishRoomEvent(roomId, event);
    }
}
