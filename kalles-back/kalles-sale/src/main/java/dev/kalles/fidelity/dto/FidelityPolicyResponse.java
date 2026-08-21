package dev.kalles.fidelity.dto;

import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.fidelity.enums.FidelityDiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FidelityPolicyResponse(
        UUID id,
        Integer objectivePoints,
        BigDecimal configuredDiscount,
        Integer valuePoint,
        FidelityDiscountType discountType,
        boolean active,
        LocalDateTime createdAt
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
