package dev.kalles.fidelity.dto;

import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.fidelity.enums.FidelityDiscountType;

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
