package com.vibify.playlist.repository;

import com.vibify.playlist.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /**
     * Fetch all playlists of a user
     */
    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Validate playlist ownership
     */
    Optional<Playlist> findByIdAndUserId(Long playlistId, Long userId);

    /**
     * Check if playlist belongs to user
     */
    boolean existsByIdAndUserId(Long playlistId, Long userId);

}