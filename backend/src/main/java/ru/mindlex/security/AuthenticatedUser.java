package ru.mindlex.security;

import java.util.UUID;

/**
 * Данные пользователя, извлечённые из JWT. Не требует обращения к базе данных.
 */
public record AuthenticatedUser(UUID id, String email, String role) {
}
