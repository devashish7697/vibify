package com.vibify.common.exception;

import com.vibify.common.exception.room_exception.*;
import com.vibify.common.globalResponse.GlobalApiResponse;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "USER_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleUserExists(UserAlreadyExistsException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "USER_ALREADY_EXISTS");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "INVALID_CREDENTIALS");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SongNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleInvalidCredentials(SongNotFoundException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "SONG_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    /// PLAYLIST EXCEPTION


    @ExceptionHandler(PlaylistException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handlePlaylistNotFound(PlaylistException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "PLAYLIST_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Playlist Not Found
     */
    @ExceptionHandler(PlaylistNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handlePlaylistNotFound(PlaylistNotFoundException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "PLAYLIST_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Duplicate Song in Playlist
     */
    @ExceptionHandler(SongAlreadyExistsInPlaylistException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleSongAlreadyExists(SongAlreadyExistsInPlaylistException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "SONG_ALREADY_EXISTS_IN_PLAYLIST");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Song Not In Playlist
     */
    @ExceptionHandler(SongNotInPlaylistException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleSongNotInPlaylist(SongNotInPlaylistException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "SONG_NOT_IN_PLAYLIST");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlaylistAccessDeniedException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleSongNotInPlaylist(PlaylistAccessDeniedException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "ACCESS DENIED");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }


    ///------- ROOM EXCEPTION -------------

    ///------- ROOM EXCEPTION -------------

    /**
     * Room Not Found
     */
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleRoomNotFound(RoomNotFoundException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "ROOM_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Room Access Denied
     */
    @ExceptionHandler(RoomAccessDeniedException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleRoomAccessDenied(RoomAccessDeniedException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "ROOM_ACCESS_DENIED");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * User Already Joined Room
     */
    @ExceptionHandler(RoomAlreadyJoinedException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleRoomAlreadyJoined(RoomAlreadyJoinedException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "ROOM_ALREADY_JOINED");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * User Not Member Of Room
     */
    @ExceptionHandler(RoomNotMemberException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleRoomNotMember(RoomNotMemberException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "ROOM_NOT_MEMBER");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Queue Item Not Found
     */
    @ExceptionHandler(QueueItemNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleQueueItemNotFound(QueueItemNotFoundException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "QUEUE_ITEM_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Playback State Not Found
     */
    @ExceptionHandler(PlaybackStateNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handlePlaybackStateNotFound(PlaybackStateNotFoundException ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(ex.getMessage(), "PLAYBACK_STATE_NOT_FOUND");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleOptimisticLock() {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error(
                        "Playback state updated by another user. Please retry.",
                        "PLAYBACK_CONFLICT"
                );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }




    /// --- GENRIC EXCEPTION ------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleGenericException(Exception ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error("Something went wrong", "INTERNAL_SERVER_ERROR");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
