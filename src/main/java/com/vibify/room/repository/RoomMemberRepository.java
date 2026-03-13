package com.vibify.room.repository;

import com.vibify.room.model.room_member.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    List<RoomMember> findByRoomIdAndLeftAtIsNull(UUID roomId);

    Optional<RoomMember> findByRoomIdAndUserIdAndLeftAtIsNull(UUID roomId, Long userId);

    boolean existsByRoomIdAndUserIdAndLeftAtIsNull(UUID roomId, Long userId);

    Optional<RoomMember> findByRoomIdAndUserId(UUID roomId, Long userId);

}
