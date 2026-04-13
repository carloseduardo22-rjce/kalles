package dev.kalles.sale.core.goal;

import dev.kalles.sale.core.entity.Goal;
import dev.kalles.sale.core.enums.goal.Periodicity;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.service.GoalAssessmentResult;
import dev.kalles.sale.core.service.GoalAssessmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("GoalAssessmentService — Domain Tests")
class GoalAssessmentServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    private final GoalAssessmentService assessmentService =
            new GoalAssessmentService(mock(SaleRepository.class));

    @Test
    @DisplayName("Should calculate achieved value and gap correctly")
    void shouldAssessGoalSuccessfully() {
        Goal goal = Goal.create(
            COMPANY_ID,
                new BigDecimal("100000.00"),
                Periodicity.MONTHLY,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );
        goal.activate();

        GoalAssessmentResult result = assessmentService.assess(goal, new BigDecimal("70000.00"));

        assertThat(result.achievedValue()).isEqualByComparingTo(new BigDecimal("70000.00"));
        assertThat(result.gap()).isEqualByComparingTo(new BigDecimal("30000.00"));
    }

    @Test
    @DisplayName("Should return zero gap when goal is exceeded")
    void shouldReturnZeroGapWhenGoalExceeded() {
        Goal goal = Goal.create(
            COMPANY_ID,
                new BigDecimal("100000.00"),
                Periodicity.MONTHLY,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );
        goal.activate();

        GoalAssessmentResult result = assessmentService.assess(goal, new BigDecimal("110000.00"));

        assertThat(result.achievedValue()).isEqualByComparingTo(new BigDecimal("110000.00"));
        assertThat(result.gap()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
