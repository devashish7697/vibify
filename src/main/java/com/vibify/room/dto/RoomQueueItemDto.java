package com.vibify.room.dto;

import com.vibify.room.model.room_queue.QueueSourceType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomQueueItemDto {

    private Long queueItemId;

    private Long songId;

    private String title;

    private String artist;

    private String coverImage;

    private Integer duration;

    private String hlsUrl;

    private Long addedBy;

    private Integer orderIndex;

    private QueueSourceType sourceType;

    private Long sourceId;

}
