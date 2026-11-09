package dev.kalles.security.service;

import java.util.UUID;

public record PairedDevice(
        UUID cashRegisterId,
        String cashRegisterCode,
        boolean paymentIntegrationConfigured,
        String deviceToken
) {
}
