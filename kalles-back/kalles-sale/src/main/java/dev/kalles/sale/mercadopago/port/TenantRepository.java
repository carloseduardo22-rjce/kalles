package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Tenant;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {
    Optional<Tenant> findById(UUID tenantId);
    void save(Tenant tenant);
}
