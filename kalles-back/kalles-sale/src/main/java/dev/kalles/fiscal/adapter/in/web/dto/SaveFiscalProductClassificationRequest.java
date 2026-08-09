package dev.kalles.fiscal.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaveFiscalProductClassificationRequest(
        @NotNull UUID productId,
        @NotBlank String ncm,
        String cest,
        String cfop,
        String cfopSale,
        String origin,
        String csosn,
        String cst,
        String unit,
        String gtin
) {
}
