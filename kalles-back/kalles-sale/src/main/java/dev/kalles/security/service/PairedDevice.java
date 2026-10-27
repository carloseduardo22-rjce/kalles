package dev.kalles.security.service;

import java.util.UUID;

public record PairedDevice(
        UUID cashRegisterId,
        String cashRegisterCode,
        String deviceToken
) {
}
