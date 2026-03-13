package com.vibify.room.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMemberDto {

    private Long userId;
    private String username;
    private String profileImage;
    private String role;
    private boolean isOnline;

}
