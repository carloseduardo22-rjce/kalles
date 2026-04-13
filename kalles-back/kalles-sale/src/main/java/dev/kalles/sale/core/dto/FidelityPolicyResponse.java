package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.enums.fidelity.FidelityDiscountType;
import dev.kalles.sale.core.entity.FidelityPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FidelityPolicyResponse(
        UUID id,
        Integer objectivePoints,
        BigDecimal configuredDiscount,
        Integer valuePoint,
        FidelityDiscountType discountType,
        boolean active,
        LocalDate createdAt
) {
    public static FidelityPolicyResponse from(FidelityPolicy policy) {
        return new FidelityPolicyResponse(
                policy.getId(),
                policy.getObjectivePoints(),
                policy.getConfiguredDiscount(),
                policy.getValuePoint(),
                policy.getDiscountType(),
                policy.isActive(),
                policy.getCreatedAt()
        );
    }
}
