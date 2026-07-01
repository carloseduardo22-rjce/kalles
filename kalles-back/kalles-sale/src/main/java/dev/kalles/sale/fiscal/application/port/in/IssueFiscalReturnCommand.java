package dev.kalles.sale.fiscal.application.port.in;

import java.util.UUID;

public record IssueFiscalReturnCommand(
        UUID tenantId,
        UUID companyId,
        UUID saleId
) {
}
