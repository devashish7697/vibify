package com.vibify.playlist.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {

    private Long id;

    private String name;

    private String coverImage;

    private Integer songCount;

}