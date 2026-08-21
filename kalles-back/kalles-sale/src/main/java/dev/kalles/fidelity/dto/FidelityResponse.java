package dev.kalles.fidelity.dto;

import dev.kalles.fidelity.entity.Fidelity;
import dev.kalles.fidelity.enums.FidelityDiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FidelityResponse(
        UUID id,
        UUID clientId,
        Integer points,
        BigDecimal availableDiscount,
        FidelityDiscountType discountType,
        LocalDateTime createdAt,
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
