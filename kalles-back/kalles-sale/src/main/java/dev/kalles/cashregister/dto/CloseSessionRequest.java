package dev.kalles.cashregister.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseSessionRequest(

        @Schema(description = "Matricula de quem libera o fechamento; exigida quando a contagem passa da tolerancia da loja", example = "OP-002")
        String authorizedOperatorCode,

        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 17, fraction = 2)
        @Schema(description = "Valor contado em dinheiro no fechamento do caixa", example = "250.00")
        BigDecimal countedCashAmount

) {}
