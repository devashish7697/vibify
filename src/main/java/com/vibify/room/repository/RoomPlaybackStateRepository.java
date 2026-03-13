package com.vibify.room.repository;


import com.vibify.room.model.playback_state.RoomPlaybackState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomPlaybackStateRepository
        extends JpaRepository<RoomPlaybackState, UUID> {
}
