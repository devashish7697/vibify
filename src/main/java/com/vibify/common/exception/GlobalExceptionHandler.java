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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleGenericException(Exception ex) {

        GlobalApiResponse<Void> response =
                GlobalApiResponse.error("Something went wrong", "INTERNAL_SERVER_ERROR");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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

}
