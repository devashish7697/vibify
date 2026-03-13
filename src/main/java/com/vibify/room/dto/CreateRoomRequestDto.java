package com.vibify.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequestDto {

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Playlist private and public is required")
    private boolean isPrivate;

}