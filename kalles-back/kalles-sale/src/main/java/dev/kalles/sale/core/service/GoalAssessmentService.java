package dev.kalles.sale.core.service;

import dev.kalles.sale.core.entity.Goal;
import dev.kalles.sale.core.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GoalAssessmentService {

    private final SaleRepository saleRepository;

    public GoalAssessmentResult assess(Goal goal, BigDecimal totalSold) {
        BigDecimal gap = goal.getTargetValue().subtract(totalSold).max(BigDecimal.ZERO);
        return new GoalAssessmentResult(totalSold, gap);
    }

    public GoalAssessmentResult autoAssess(Goal goal) {
        LocalDateTime start = goal.getStartDate().atStartOfDay();
        LocalDateTime end = goal.getEndDate().plusDays(1).atStartOfDay();
        BigDecimal totalSold = saleRepository.sumCompletedTotalsBetween(start, end);
        return assess(goal, totalSold);
    }
}
