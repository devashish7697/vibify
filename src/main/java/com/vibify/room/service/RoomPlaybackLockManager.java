package com.vibify.room.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RoomPlaybackLockManager {

    private final ConcurrentHashMap<UUID, ReentrantLock> locks =
            new ConcurrentHashMap<>();

    public ReentrantLock getLock(UUID roomId) {
        return locks.computeIfAbsent(roomId, id -> new ReentrantLock(true));
    }

    public void releaseLockIfUnused(UUID roomId) {
        ReentrantLock lock = locks.get(roomId);
        if (lock != null && !lock.hasQueuedThreads()) {
            locks.remove(roomId, lock);
        }
    }

}