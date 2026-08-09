package dev.kalles.fiscal.domain;

import java.time.Instant;
import java.util.UUID;

public record FiscalCertificate(
        UUID id,
        UUID tenantId,
        UUID companyId,
        Instant expiresAt,
        boolean active,
        String protectedContent,
        String protectedPassword
) {
    public boolean isValidAt(Instant instant) {
        return active && expiresAt != null && expiresAt.isAfter(instant);
    }
}
