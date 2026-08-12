package dev.kalles.security.event;

public record VerificationCodeIssued(
        String email,
        String name,
        String code,
        int expiresInMinutes
) {
}
