package dev.kalles.goal.service;

import dev.kalles.core.repository.SaleRepository;
import dev.kalles.goal.entity.Goal;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        BigDecimal totalSold = saleRepository.sumCompletedTotalsBetween(companyId, start, end);
        return assess(goal, totalSold);
    }
}
