package dev.kalles.sale.fiscal.adapter.in.web.dto;

import dev.kalles.sale.fiscal.domain.FiscalProductClassification;

import java.util.UUID;

public record FiscalProductClassificationResponse(
        UUID id,
        UUID tenantId,
        UUID companyId,
        UUID productId,
        String ncm,
        String cest,
        String cfop,
        String cfopSale,
        String origin,
        String csosn,
        String cst,
        String unit,
        String gtin
) {
    public static FiscalProductClassificationResponse from(FiscalProductClassification classification) {
        return new FiscalProductClassificationResponse(classification.id(), classification.tenantId(),
                classification.companyId(), classification.productId(), classification.ncm(), classification.cest(),
                classification.cfop(), classification.cfopSale(), classification.origin(), classification.csosn(),
                classification.cst(), classification.unit(), classification.gtin());
    }
}
