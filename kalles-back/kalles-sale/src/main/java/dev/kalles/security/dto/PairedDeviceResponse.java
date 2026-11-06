package dev.kalles.security.dto;

import java.util.UUID;

public record PairedDeviceResponse(
        UUID cashRegisterId,
        String cashRegisterCode
) {
}
