package com.vibify.user.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 255)
    private String bio;

    @Size(max = 255)
    private String profileImage;

}