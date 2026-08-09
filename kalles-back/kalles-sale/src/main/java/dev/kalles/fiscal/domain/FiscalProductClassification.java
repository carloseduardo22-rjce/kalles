package dev.kalles.fiscal.domain;

import java.util.UUID;

public record FiscalProductClassification(
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
    public FiscalProductClassification(
            UUID id,
            UUID tenantId,
            UUID companyId,
            UUID productId,
            String ncm,
            String cest,
            String cfop
    ) {
        this(id, tenantId, companyId, productId, ncm, cest, cfop, cfop, null, null, null, null, null);
    }

    public boolean isCompatibleWith(FiscalTaxRegime taxRegime) {
        if (taxRegime == FiscalTaxRegime.SIMPLES_NACIONAL) {
            return csosn != null && !csosn.isBlank() && (cst == null || cst.isBlank());
        }
        return cst != null && !cst.isBlank() && (csosn == null || csosn.isBlank());
    }
}
