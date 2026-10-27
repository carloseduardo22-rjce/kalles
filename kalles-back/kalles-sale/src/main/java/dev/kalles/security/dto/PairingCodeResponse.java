package dev.kalles.security.dto;

import java.time.LocalDateTime;

public record PairingCodeResponse(
        String code,
        LocalDateTime expiresAt
) {
}
