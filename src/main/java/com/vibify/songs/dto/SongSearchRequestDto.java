package com.vibify.songs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongSearchRequestDto {

    @NotBlank(message = "Search query required")
    private String query;

    @Min(value = 0)
    private Integer page;

    @Min(value = 1)
    private Integer size;
}
