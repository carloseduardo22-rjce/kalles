package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalSale;

import java.util.Optional;
import java.util.UUID;

public interface FiscalSaleReader {
    Optional<FiscalSale> findByIdForTenantAndCompany(UUID saleId, UUID tenantId, UUID companyId);
}
