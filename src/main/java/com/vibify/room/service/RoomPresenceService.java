package com.vibify.room.service;

import java.util.UUID;

public interface RoomPresenceService {

    void joinPresence(UUID roomId, Long userId);

    void heartbeat(UUID roomId, Long userId);

    void leavePresence(UUID roomId, Long userId);

}
