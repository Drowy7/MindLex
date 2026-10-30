package ru.mindlex.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Укажите e-mail") String email,
        @NotBlank(message = "Укажите пароль") String password
) {
}
