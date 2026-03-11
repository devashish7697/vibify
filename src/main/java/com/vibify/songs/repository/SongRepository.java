package com.vibify.songs.repository;

import com.vibify.songs.model.SongStatus;
import com.vibify.songs.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {

    Page<Song> findByStatus(SongStatus status, Pageable pageable);

    Page<Song> findByTitleContainingIgnoreCaseAndStatus(
            String title,
            SongStatus status,
            Pageable pageable
    );

    Page<Song> findByArtistContainingIgnoreCaseAndStatus(
            String artist,
            SongStatus status,
            Pageable pageable
    );

    Page<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseAndStatus(
            String title,
            String artist,
            SongStatus status,
            Pageable pageable
    );
}