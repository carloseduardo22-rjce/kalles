package dev.kalles.fidelity.dto;

import dev.kalles.fidelity.enums.FidelityDiscountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FidelityPolicyRequest(
        @NotNull @Positive Integer objectivePoints,
        @NotNull @Positive BigDecimal configuredDiscount,
        @NotNull @Positive Integer valuePoint,
        FidelityDiscountType discountType
) {
    public FidelityPolicyRequest {
        if (configuredDiscount != null) {
            configuredDiscount = configuredDiscount.stripTrailingZeros();
        }
        if (discountType == null) {
            discountType = FidelityDiscountType.FIXED;
        }
        if (discountType == FidelityDiscountType.PERCENTAGE
                && configuredDiscount != null
                && configuredDiscount.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("O desconto percentual nao pode ultrapassar 100%.");
        }
    }
}
