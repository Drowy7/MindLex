package ru.mindlex.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Преобразование исключений в единый JSON-ответ.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Ключ MDC с числовым кодом ответа (см. {@code LogFields.HTTP_STATUS}). */
    private static final String STATUS_MDC_KEY = "http.response.status_code";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("Объект не найден: {} {} ({})", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        log.warn("Конфликт: {} {} ({})", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("Нарушено бизнес-правило: {} {} ({})", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        // Отказ в аутентификации относится к событиям безопасности: уровень WARN, без стека.
        log.warn("Отказ в аутентификации: {} ({} {})", ex.getMessage(), request.getMethod(), request.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "Неверный e-mail или пароль", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Отказано в доступе: {} {} ({})", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Недостаточно прав", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        log.warn("Отклонены данные запроса {} {}: поля {}", request.getMethod(), request.getRequestURI(), fields.keySet());
        return badRequest(ApiError.validation(request.getRequestURI(), fields), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(violation -> fields.put(violation.getPropertyPath().toString(), violation.getMessage()));
        log.warn("Нарушено ограничение {} {}: {}", request.getMethod(), request.getRequestURI(), fields.keySet());
        return badRequest(ApiError.validation(request.getRequestURI(), fields), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Нарушение целостности данных: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Нарушено ограничение целостности данных", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Необработанная ошибка при обработке {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request);
    }

    /**
     * Ответ 400 на ошибку валидации. Код статуса проставляется в MDC так же, как в {@link #build},
     * чтобы в журнале у любого отказа было числовое поле {@code http.response.status_code}.
     */
    private ResponseEntity<ApiError> badRequest(ApiError error, HttpServletRequest request) {
        MDC.put(STATUS_MDC_KEY, Integer.toString(HttpStatus.BAD_REQUEST.value()));
        try {
            return ResponseEntity.badRequest().body(error);
        } finally {
            MDC.remove(STATUS_MDC_KEY);
        }
    }

    /**
     * Единый ответ об ошибке. Числовой код статуса дополнительно кладётся в MDC, чтобы в журнале
     * появилось поле {@code http.response.status_code} — по нему удобно строить stats-запросы.
     * Ключ снимается сразу после формирования ответа, чтобы не «прилипать» к последующим записям:
     * поток Tomcat переиспользуется.
     */
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        MDC.put(STATUS_MDC_KEY, Integer.toString(status.value()));
        try {
            return ResponseEntity.status(status)
                    .body(ApiError.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
        } finally {
            MDC.remove(STATUS_MDC_KEY);
        }
    }
}
