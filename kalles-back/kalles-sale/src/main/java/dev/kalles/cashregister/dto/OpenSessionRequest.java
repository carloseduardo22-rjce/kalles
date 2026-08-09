package dev.kalles.cashregister.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenSessionRequest(
    @NotBlank(message = "Codigo do caixa obrigatorio")
    String cashRegisterCode,

    @NotBlank(message = "Codigo do operador obrigatorio")
    String operatorCode,

    @NotNull(message = "Valor inicial obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor inicial nao pode ser negativo")
    BigDecimal initialAmount,

    Boolean allowCashOnlyOperation
) {
    public boolean shouldAllowCashOnlyOperation() {
        return Boolean.TRUE.equals(allowCashOnlyOperation);
    }
}
