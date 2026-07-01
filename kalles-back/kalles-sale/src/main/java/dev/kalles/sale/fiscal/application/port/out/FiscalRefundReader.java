package dev.kalles.sale.fiscal.application.port.out;

import java.util.UUID;

public interface FiscalRefundReader {
    boolean hasConfirmedRefund(UUID tenantId, UUID companyId, UUID saleId);
}
