package com.vibify.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderQueueRequestDto {

    @NotNull(message = "Queue item id is required")
    private Long queueItemId;

    @NotNull(message = "New order index is required")
    private Integer newOrderIndex;

}