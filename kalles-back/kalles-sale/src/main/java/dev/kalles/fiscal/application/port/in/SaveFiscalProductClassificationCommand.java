package dev.kalles.fiscal.application.port.in;

import java.util.UUID;

public record SaveFiscalProductClassificationCommand(
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
    public SaveFiscalProductClassificationCommand(
            UUID tenantId,
            UUID companyId,
            UUID productId,
            String ncm,
            String cest,
            String cfop
    ) {
        this(tenantId, companyId, productId, ncm, cest, cfop, cfop, null, null, null, null, null);
    }
}
