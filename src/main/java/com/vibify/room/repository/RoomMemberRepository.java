package com.vibify.room.repository;

import com.vibify.room.model.room_member.RoomMember;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    List<RoomMember> findByRoomIdAndLeftAtIsNull(UUID roomId);

    Optional<RoomMember> findByRoomIdAndUserIdAndLeftAtIsNull(UUID roomId, Long userId);

    boolean existsByRoomIdAndUserIdAndLeftAtIsNull(UUID roomId, Long userId);

    Optional<RoomMember> findByRoomIdAndUserId(UUID roomId, Long userId);

    Optional<RoomMember> findFirstByUserIdAndLeftAtIsNull(Long userId);

    boolean existsByUserIdAndLeftAtIsNull(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rm FROM RoomMember rm WHERE rm.userId = :userId AND rm.leftAt IS NULL")
    Optional<RoomMember> findActiveRoomForUpdate(@Param("userId") Long userId);

}
