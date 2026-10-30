package ru.mindlex.auth;

import ru.mindlex.user.UserResponse;

public record AuthResponse(String accessToken,
                           String tokenType,
                           long expiresInSeconds,
                           UserResponse user) {

    public static AuthResponse of(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
