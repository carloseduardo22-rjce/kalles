package dev.kalles.sale.core.goal;

import dev.kalles.sale.core.entity.Goal;
import dev.kalles.sale.core.enums.goal.GoalStatus;
import dev.kalles.sale.core.enums.goal.Periodicity;
import dev.kalles.sale.core.exception.GoalDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Goal — Domain Tests")
class GoalTest {

        private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @Nested
    @DisplayName("Goal Creation")
    class GoalCreation {

        @Test
        @DisplayName("Should create a Goal with DRAFT status and correct data")
        void shouldCreateGoalSuccessfully() {
            Goal goal = Goal.create(
                    COMPANY_ID,
                    new BigDecimal("100000.00"),
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            );

            assertThat(goal.getTargetValue()).isEqualByComparingTo(new BigDecimal("100000.00"));
            assertThat(goal.getPeriodicity()).isEqualTo(Periodicity.MONTHLY);
            assertThat(goal.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            assertThat(goal.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 31));
            assertThat(goal.getStatus()).isEqualTo(GoalStatus.DRAFT);
        }

        @Test
        @DisplayName("Should reject Goal with null target value")
        void shouldRejectNullTargetValue() {
            assertThatThrownBy(() -> Goal.create(
                    COMPANY_ID,
                    null,
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            ))
                    .isInstanceOf(GoalDomainException.class)
                    .hasMessage("Target value is required");
        }

        @Test
        @DisplayName("Should reject Goal with zero or negative target value")
        void shouldRejectNonPositiveTargetValue() {
            assertThatThrownBy(() -> Goal.create(
                    COMPANY_ID,
                    BigDecimal.ZERO,
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            ))
                    .isInstanceOf(GoalDomainException.class)
                    .hasMessage("Target value must be positive");
        }

        @Test
        @DisplayName("Should reject Goal with end date before start date")
        void shouldRejectInvalidPeriod() {
            assertThatThrownBy(() -> Goal.create(
                    COMPANY_ID,
                    new BigDecimal("100000.00"),
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 31),
                    LocalDate.of(2026, 3, 1)
            ))
                    .isInstanceOf(GoalDomainException.class)
                    .hasMessage("End date must be after start date");
        }
    }

    @Nested
    @DisplayName("Active Goal Immutability")
    class ActiveGoalImmutability {

        @Test
        @DisplayName("Should reject target value change on ACTIVE Goal")
        void shouldRejectTargetValueChangeOnActiveGoal() {
            Goal goal = Goal.create(
                    COMPANY_ID,
                    new BigDecimal("100000.00"),
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            );
            goal.activate();

            assertThatThrownBy(() -> goal.changeTargetValue(new BigDecimal("120000.00")))
                    .isInstanceOf(GoalDomainException.class)
                    .hasMessage("Active goal cannot have its target value changed");
        }

        @Test
        @DisplayName("Should reject period change on ACTIVE Goal")
        void shouldRejectPeriodChangeOnActiveGoal() {
            Goal goal = Goal.create(
                    COMPANY_ID,
                    new BigDecimal("100000.00"),
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            );
            goal.activate();

            assertThatThrownBy(() -> goal.changePeriod(
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 4, 30)
            ))
                    .isInstanceOf(GoalDomainException.class)
                    .hasMessage("Active goal cannot have its dates changed");
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Should transition from DRAFT to ACTIVE")
        void shouldActivateGoal() {
            Goal goal = Goal.create(
                    COMPANY_ID,
                    new BigDecimal("100000.00"),
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            );

            goal.activate();

            assertThat(goal.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to CLOSED")
        void shouldCloseGoal() {
            Goal goal = Goal.create(
                    COMPANY_ID,
                    new BigDecimal("100000.00"),
                    Periodicity.MONTHLY,
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31)
            );
            goal.activate();

            goal.close();

            assertThat(goal.getStatus()).isEqualTo(GoalStatus.CLOSED);
        }
    }
}
