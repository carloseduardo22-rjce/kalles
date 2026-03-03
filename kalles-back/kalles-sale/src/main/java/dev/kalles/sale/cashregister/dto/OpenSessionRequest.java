package dev.kalles.sale.cashregister.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenSessionRequest(
    @NotBlank(message = "Código do caixa obrigatório")
    String cashRegisterCode,

    @NotBlank(message = "Código do operador obrigatório")
    String operatorCode,

    @NotNull(message = "Valor inicial obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor inicial não pode ser negativo")
    BigDecimal initialAmount
) {
}
