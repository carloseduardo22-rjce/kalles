package dev.kalles.sale.payment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaymentTerminalMapping(
        UUID id,
        UUID tenantId,
        UUID companyId,
        UUID cashRegisterId,
        PaymentProvider provider,
        String terminalSerial,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public PaymentTerminalMapping {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(cashRegisterId, "cashRegisterId is required");
        Objects.requireNonNull(provider, "provider is required");
        if (terminalSerial == null || terminalSerial.isBlank()) {
            throw new IllegalArgumentException("terminalSerial is required");
        }
        terminalSerial = normalizeSerial(terminalSerial);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public static String normalizeSerial(String serial) {
        return serial == null ? null : serial.trim().toUpperCase();
    }

    public PaymentTerminalMapping withTerminalSerial(String newTerminalSerial) {
        return new PaymentTerminalMapping(
                id,
                tenantId,
                companyId,
                cashRegisterId,
                provider,
                newTerminalSerial,
                active,
                createdAt,
                Instant.now()
        );
    }
}
