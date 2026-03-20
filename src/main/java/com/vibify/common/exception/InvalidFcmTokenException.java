package com.vibify.common.exception;


public class InvalidFcmTokenException extends RuntimeException {

    public InvalidFcmTokenException(String message) {
        super(message);
    }
}