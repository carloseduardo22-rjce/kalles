package dev.kalles.security.dto;

import jakarta.validation.constraints.NotBlank;

public record PairDeviceRequest(
        @NotBlank String pairingCode
) {
}
