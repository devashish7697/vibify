package com.vibify.notification.service;


import com.vibify.room.model.playback_state.RoomPlaybackState;

public interface NotificationService {

    void sendPlaybackUpdate(RoomPlaybackState state, Long triggeredByUserId);

}