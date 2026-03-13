package com.vibify.room.repository;

import com.vibify.room.model.room_entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);


}
