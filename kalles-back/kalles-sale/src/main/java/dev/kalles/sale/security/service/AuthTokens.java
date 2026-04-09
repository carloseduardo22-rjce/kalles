package dev.kalles.sale.security.service;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
