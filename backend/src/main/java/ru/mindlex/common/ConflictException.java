package ru.mindlex.common;

/**
 * Конфликт состояния (например, e-mail уже занят) — преобразуется в HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
