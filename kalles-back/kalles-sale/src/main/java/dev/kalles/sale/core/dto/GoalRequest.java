package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.enums.goal.Periodicity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(

    @NotNull(message = "O valor alvo é obrigatório")
    @Positive(message = "O valor alvo deve ser maior que zero")
    @Schema(description = "Valor alvo de faturamento", example = "100000.00")
    BigDecimal targetValue,

    @NotNull(message = "A periodicidade é obrigatória")
    @Schema(description = "Periodicidade da meta: WEEKLY ou MONTHLY")
    Periodicity periodicity,

    @NotNull(message = "A data de início é obrigatória")
    @Schema(description = "Data de início do período", example = "2026-03-01")
    LocalDate startDate,

    @NotNull(message = "A data de fim é obrigatória")
    @Schema(description = "Data de fim do período", example = "2026-03-31")
    LocalDate endDate
) {
    @AssertTrue(message = "A data de fim deve ser igual ou posterior Ã  data de inÃ­cio")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
