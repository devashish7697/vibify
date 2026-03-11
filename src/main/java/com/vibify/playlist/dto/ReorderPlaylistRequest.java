package com.vibify.playlist.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPlaylistRequest {

    @NotEmpty(message = "Song order list cannot be empty")
    private List<Long> songIds;

}