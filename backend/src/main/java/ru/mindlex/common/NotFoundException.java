package ru.mindlex.common;

/**
 * Сущность не найдена — преобразуется в HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " с идентификатором " + id + " не найден");
    }
}
