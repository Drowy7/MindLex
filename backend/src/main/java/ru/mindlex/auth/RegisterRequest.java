package ru.mindlex.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Регистрация нового пользователя.
 */
public record RegisterRequest(
        @NotBlank(message = "Укажите e-mail")
        @Email(message = "Некорректный e-mail")
        String email,

        @NotBlank(message = "Укажите пароль")
        @Size(min = 6, max = 72, message = "Пароль должен быть от 6 до 72 символов")
        String password,

        @NotBlank(message = "Укажите имя")
        @Size(max = 120, message = "Имя слишком длинное")
        String displayName,

        @Size(max = 8, message = "Код языка слишком длинный")
        String language
) {
}
