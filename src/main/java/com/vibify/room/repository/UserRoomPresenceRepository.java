package com.vibify.room.repository;

import com.vibify.room.model.room_presence.UserRoomPresence;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoomPresenceRepository
        extends JpaRepository<UserRoomPresence, Long> {

    Optional<UserRoomPresence> findByRoomIdAndUserId(UUID roomId, Long userId);

    boolean existsByRoomIdAndUserIdAndIsActiveTrue(UUID roomId, Long userId);

    boolean existsByRoomIdAndUserId(UUID roomId, Long userId);

    @Modifying
    @Query("""
        update UserRoomPresence p
        set p.lastPing = :time, p.isActive = true
        where p.roomId = :roomId and p.userId = :userId
    """)
    void updateHeartbeat(UUID roomId,
                         Long userId,
                         LocalDateTime time);

    @Modifying
    @Query("""
        update UserRoomPresence p
        set p.isActive = false
        where p.roomId = :roomId and p.userId = :userId
    """)
    void markInactive(UUID roomId, Long userId);

    @Modifying
    @Query("""
        update UserRoomPresence p
        set p.isActive = false
        where p.lastPing < :cutoff and p.isActive = true
    """)
    int markInactiveUsers(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
        SELECT COUNT(p)
        FROM UserRoomPresence p
        WHERE p.roomId = :roomId
        AND p.isActive = true
    """)
    long countActiveUsers(UUID roomId);

    @Query("""
    SELECT p
    FROM UserRoomPresence p
    WHERE p.lastPing < :cutoff
    AND p.isActive = true
    """)
    List<UserRoomPresence> findInactiveUsers(@Param("cutoff") LocalDateTime cutoff);
}