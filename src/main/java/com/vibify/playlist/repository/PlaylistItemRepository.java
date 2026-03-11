package com.vibify.playlist.repository;

import com.vibify.playlist.model.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    /**
     * Fetch playlist songs ordered by position
     */
    List<PlaylistItem> findByPlaylistIdOrderByOrderIndexAsc(Long playlistId);

    /**
     * Check if song already exists in playlist
     */
    Optional<PlaylistItem> findByPlaylistIdAndSongId(Long playlistId, Long songId);

    /**
     * Delete a song from playlist
     */
    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    /**
     * Count songs inside playlist
     */
    long countByPlaylistId(Long playlistId);

    /**
     * Find max position inside playlist
     */
    Optional<PlaylistItem> findTopByPlaylistIdOrderByOrderIndexDesc(Long playlistId);

}