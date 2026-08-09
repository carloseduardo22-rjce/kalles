package dev.kalles.goal.service;

import java.math.BigDecimal;

public record GoalAssessmentResult(BigDecimal achievedValue, BigDecimal gap) {
}
