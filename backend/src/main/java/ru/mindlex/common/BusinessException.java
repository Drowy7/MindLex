package ru.mindlex.common;

/**
 * Нарушение бизнес-правила (например, попытка отвечать в завершённой сессии) — HTTP 422.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
