package dev.kalles.fiscal.adapter.in.web.dto;

import dev.kalles.fiscal.domain.FiscalCertificate;

import java.time.Instant;
import java.util.UUID;

public record FiscalCertificateResponse(
        UUID id,
        UUID tenantId,
        UUID companyId,
        Instant expiresAt,
        boolean active
) {
    public static FiscalCertificateResponse from(FiscalCertificate certificate) {
        return new FiscalCertificateResponse(certificate.id(), certificate.tenantId(), certificate.companyId(),
                certificate.expiresAt(), certificate.active());
    }
}
