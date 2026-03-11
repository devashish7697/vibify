package com.vibify.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    @Size(max = 255, message = "Playlist name cannot exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

}
