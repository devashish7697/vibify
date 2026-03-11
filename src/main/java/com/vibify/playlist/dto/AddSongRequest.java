package com.vibify.playlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSongRequest {

    @NotNull(message = "Song ID is required")
    private Long songId;

}