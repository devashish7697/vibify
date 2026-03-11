package com.vibify.playlist.model;

import com.vibify.songs.model.Song;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "playlist_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_playlist_song",
                        columnNames = {"playlist_id", "song_id"}
                )
        },
        indexes = {
                @Index(name = "idx_playlist_items_playlist", columnList = "playlist_id"),
                @Index(name = "idx_playlist_items_order", columnList = "playlist_id,order_index"),
                @Index(name = "idx_playlist_items_song", columnList = "song_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Playlist reference
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    /**
     * Song reference
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /**
     * Order inside playlist
     */
    @Column(name = "order_index", nullable = false)
    private Long orderIndex;

    /**
     * When song was added
     */
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    public void prePersist() {
        this.addedAt = LocalDateTime.now();
    }

}