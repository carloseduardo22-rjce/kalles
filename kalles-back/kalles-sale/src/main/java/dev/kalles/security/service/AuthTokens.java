package dev.kalles.security.service;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
