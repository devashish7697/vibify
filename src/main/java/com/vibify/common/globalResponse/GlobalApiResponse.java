package com.vibify.common.globalResponse;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class GlobalApiResponse<T> {

    private final String message;

    private final T data;

    private final String errorCode;

    private final Instant timestamp;

    public static <T> GlobalApiResponse<T> success(String message, T data) {
        return GlobalApiResponse.<T>builder()
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> GlobalApiResponse<T> success(T data) {
        return GlobalApiResponse.<T>builder()
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> GlobalApiResponse<T> error(String message, String errorCode) {
        return GlobalApiResponse.<T>builder()
                .message(message)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }
}