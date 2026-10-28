package ru.mindlex.user;

import ru.mindlex.user.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName, String role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(),
                user.getCreatedAt());
    }
}
