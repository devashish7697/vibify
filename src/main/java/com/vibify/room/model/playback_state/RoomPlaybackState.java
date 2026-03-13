package com.vibify.room.model.playback_state;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "room_playback_state")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlaybackState {

    @Id
    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "song_id")
    private Long songId;

    @Column(name = "queue_item_id")
    private Long queueItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private PlaybackSourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "started_at")
    private Long startedAt;

    @Column(name = "offset_millis")
    private Long offsetMillis;

    @Enumerated(EnumType.STRING)
    @Column(name = "playback_status")
    private PlaybackStatus status;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "last_action_by")
    private Long lastActionBy;

    @Column(name = "updated_at")
    private Long updatedAt;
}
