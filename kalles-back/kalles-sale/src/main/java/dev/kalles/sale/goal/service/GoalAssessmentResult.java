package dev.kalles.sale.goal.service;

import java.math.BigDecimal;

public record GoalAssessmentResult(BigDecimal achievedValue, BigDecimal gap) {
}
