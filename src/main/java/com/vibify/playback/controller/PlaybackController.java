package com.vibify.playback.controller;

import com.vibify.auth.security.UserPrincipal;
import com.vibify.playback.dto.PlaybackRequest;
import com.vibify.playback.service.PlaybackFacade;
import com.vibify.common.globalResponse.GlobalApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playback")
@RequiredArgsConstructor
public class PlaybackController {

    private final PlaybackFacade playbackFacade;

    @PostMapping("/play")
    public GlobalApiResponse<Object> play(
            @Valid @RequestBody PlaybackRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        if (userPrincipal == null) {
            throw new RuntimeException("Unauthorized");
        }

        Long userId = userPrincipal.getId();
        Object response = playbackFacade.play(request, userId);

        return GlobalApiResponse.success(response);
    }
}