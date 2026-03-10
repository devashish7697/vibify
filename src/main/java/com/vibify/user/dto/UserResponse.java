package com.vibify.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String username;

    private String email;

    private String profileImage;

    private String bio;

    private Boolean isOnline;

}