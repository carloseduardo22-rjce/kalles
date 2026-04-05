package dev.kalles.sale.cashregister.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseSessionRequest(

        @NotBlank
        @Schema(description = "Codigo do operador autorizador do fechamento", example = "OP-002")
        String authorizedOperatorCode,

        @NotNull
        @DecimalMin(value = "0.00")
        @Schema(description = "Valor contado em dinheiro no fechamento do caixa", example = "250.00")
        BigDecimal countedCashAmount

) {}
