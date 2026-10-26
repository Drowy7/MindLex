package ru.mindlex.common;

import java.time.Instant;
import java.util.Map;

/**
 * Единый формат ошибки REST-API.
 */
public record ApiError(Instant timestamp,
                       int status,
                       String error,
                       String message,
                       String path,
                       Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError validation(String path, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), 400, "Bad Request", "Ошибка валидации запроса", path, fieldErrors);
    }
}
