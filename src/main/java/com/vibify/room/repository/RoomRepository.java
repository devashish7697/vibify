package com.vibify.room.repository;

import com.vibify.room.model.room_entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);

    @Query("""
    SELECT r
    FROM Room r
    WHERE r.status = 'ACTIVE'
    AND r.lastActivityAt < :cutoff
    """)
    List<Room> findInactiveRooms(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
    SELECT r
    FROM Room r
    WHERE r.status = 'ENDED'
    AND r.endedAt < :cutoff
    """)
    List<Room> findRoomsToDelete(@Param("cutoff") LocalDateTime cutoff);

}
