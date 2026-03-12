package dev.kalles.sale.core.service;

import java.math.BigDecimal;

public record GoalAssessmentResult(BigDecimal achievedValue, BigDecimal gap) {
}
