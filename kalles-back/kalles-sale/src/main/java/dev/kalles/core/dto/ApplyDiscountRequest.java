package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ApplyDiscountRequest(
    @Schema(description = "ID do item da venda ao qual o desconto será aplicado")
    @NotNull UUID itemId,

    @Schema(description = "Valor absoluto do desconto em reais (não percentual). Não pode ser negativo nem exceder o preço unitário do item.", example = "5.00")
    @NotNull @Positive @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal discountAmount
) {}
