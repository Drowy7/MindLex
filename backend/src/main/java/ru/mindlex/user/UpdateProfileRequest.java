package ru.mindlex.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Укажите имя")
        @Size(max = 120, message = "Имя слишком длинное")
        String displayName
) {
}
