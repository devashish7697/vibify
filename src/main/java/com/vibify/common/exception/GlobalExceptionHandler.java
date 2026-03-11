package com.vibify.common.exception;

import com.vibify.common.globalResponse.GlobalApiResponse;
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





    /// --- GENRIC EXCEPTION ------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleGenericException(Exception ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error("Something went wrong", "INTERNAL_SERVER_ERROR");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
