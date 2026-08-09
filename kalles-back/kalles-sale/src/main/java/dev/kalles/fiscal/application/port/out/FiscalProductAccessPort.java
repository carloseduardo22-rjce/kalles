package dev.kalles.fiscal.application.port.out;

import java.util.UUID;

public interface FiscalProductAccessPort {
    boolean existsProductByTenant(UUID tenantId, UUID productId);
}
