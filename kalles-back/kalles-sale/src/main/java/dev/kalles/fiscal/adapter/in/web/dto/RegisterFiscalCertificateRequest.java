package dev.kalles.fiscal.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RegisterFiscalCertificateRequest(
        @NotBlank String certificateBase64,
        @NotBlank String password,
        @NotNull Instant expiresAt
) {
}
