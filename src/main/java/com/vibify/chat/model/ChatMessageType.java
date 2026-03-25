package com.vibify.chat.model;

public enum ChatMessageType {

    TEXT,      // normal message
    IMAGE,     // image file
    VIDEO,     // video file
    AUDIO,     // voice note / audio
    SYSTEM     // system generated (join/leave/etc)
}