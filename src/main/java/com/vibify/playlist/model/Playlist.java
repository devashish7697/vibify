package com.vibify.playlist.model;

import com.vibify.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "playlists",
        indexes = {
                @Index(name = "idx_playlist_user", columnList = "user_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     // Owner of playlist
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

     // Playlist name
    @Column(nullable = false, length = 255)
    private String name;


     // Optional description
    @Column(length = 500)
    private String description;

    // Playlist cover image
     // (Default = first song cover)
    @Column(name = "cover_image", length = 500)
    private String coverImage;

     // Cached count for fast reads
    @Column(name = "song_count", nullable = false)
    @Builder.Default
    private Integer songCount = 0;

    /**
     * Created timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
