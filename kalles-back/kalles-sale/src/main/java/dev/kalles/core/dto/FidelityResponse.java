package dev.kalles.core.dto;

import dev.kalles.core.enums.fidelity.FidelityDiscountType;
import dev.kalles.core.entity.Fidelity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FidelityResponse(
        UUID id,
        UUID clientId,
        Integer points,
        BigDecimal availableDiscount,
        FidelityDiscountType discountType,
        LocalDate createdAt,
        boolean expired
) {
    public static FidelityResponse from(Fidelity fidelity) {
        return new FidelityResponse(
                fidelity.getId(),
                fidelity.getClient().getId(),
                fidelity.getPoints(),
                fidelity.getAvailableDiscount(),
                fidelity.getPolicy().getDiscountType(),
                fidelity.getCreatedAt(),
                fidelity.isExpired() || fidelity.isActuallyExpired()
        );
    }
}
