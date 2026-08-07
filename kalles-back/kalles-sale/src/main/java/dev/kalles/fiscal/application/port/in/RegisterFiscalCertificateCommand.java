package dev.kalles.fiscal.application.port.in;

import java.time.Instant;
import java.util.UUID;

public record RegisterFiscalCertificateCommand(
        UUID tenantId,
        UUID companyId,
        String certificateBase64,
        String password,
        Instant expiresAt
) {
}
