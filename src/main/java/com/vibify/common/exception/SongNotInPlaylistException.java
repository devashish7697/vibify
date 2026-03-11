package com.vibify.common.exception;


public class SongNotInPlaylistException extends PlaylistException {

    public SongNotInPlaylistException(String message) {
        super(message);
    }

}