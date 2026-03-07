package dev.kalles.sale.core.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FidelityPolicyRequest(
        @NotNull @Positive Integer objectivePoints,
        @NotNull @Positive BigDecimal configuredDiscount,
        @NotNull @Positive Integer valuePoint
) {}
