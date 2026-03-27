package com.vibify.playback.service;

import com.vibify.playback.dto.PlaybackRequest;
import com.vibify.playback.dto.PlaybackResponse;

public interface PlaybackFacade {

    PlaybackResponse play(PlaybackRequest request, Long userId);

}
