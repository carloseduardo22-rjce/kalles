package dev.kalles.sale.goal.dto;

import dev.kalles.sale.goal.entity.Goal;
import dev.kalles.sale.goal.enums.GoalStatus;
import dev.kalles.sale.goal.enums.Periodicity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(

    @Schema(description = "Identificador único da meta")
    UUID id,

    @Schema(description = "Valor alvo de faturamento")
    BigDecimal targetValue,

    @Schema(description = "Periodicidade: WEEKLY ou MONTHLY")
    Periodicity periodicity,

    @Schema(description = "Data de início do período")
    LocalDate startDate,

    @Schema(description = "Data de fim do período")
    LocalDate endDate,

    @Schema(description = "Status da meta: DRAFT, ACTIVE ou CLOSED")
    GoalStatus status
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
            goal.getId(),
            goal.getTargetValue(),
            goal.getPeriodicity(),
            goal.getStartDate(),
            goal.getEndDate(),
            goal.getStatus()
        );
    }
}
