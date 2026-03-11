package com.vibify.songs.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "songs",
        indexes = {
                @Index(name = "idx_song_title", columnList = "title"),
                @Index(name = "idx_song_artist", columnList = "artist")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private Integer duration;

    private String genre;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "hls_url", length = 500)
    private String hlsUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongStatus status;

    @Column(name = "processing_error", length = 1000)
    private String processingError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = SongStatus.PROCESSING;
    }
}