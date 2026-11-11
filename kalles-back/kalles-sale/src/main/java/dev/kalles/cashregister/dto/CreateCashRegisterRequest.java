package dev.kalles.cashregister.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCashRegisterRequest(
    @NotBlank(message = "A descrição do caixa é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    String description,

    java.util.UUID companyId
) {
}
