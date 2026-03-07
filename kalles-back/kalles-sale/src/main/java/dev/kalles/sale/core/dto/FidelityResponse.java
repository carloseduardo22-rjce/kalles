package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.entity.Fidelity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FidelityResponse(
        UUID id,
        UUID clientId,
        Integer points,
        BigDecimal availableDiscount,
        LocalDate createdAt,
        boolean expired
) {
    public static FidelityResponse from(Fidelity fidelity) {
        return new FidelityResponse(
                fidelity.getId(),
                fidelity.getClient().getId(),
                fidelity.getPoints(),
                fidelity.getAvailableDiscount(),
                fidelity.getCreatedAt(),
                fidelity.isExpired() || fidelity.isActuallyExpired()
        );
    }
}
