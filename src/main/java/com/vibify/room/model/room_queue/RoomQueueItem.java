package com.vibify.room.model.room_queue;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "room_queue_items",
        indexes = {
                @Index(name = "idx_room_queue_room", columnList = "room_id"),
                @Index(name = "idx_room_queue_order", columnList = "room_id,order_index")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomQueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Column(name = "added_by", nullable = false)
    private Long addedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private QueueSourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "created_at")
    private Long createdAt;
}
