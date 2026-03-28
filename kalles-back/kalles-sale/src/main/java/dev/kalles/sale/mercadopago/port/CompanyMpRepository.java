package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Company;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMpRepository {
    Optional<Company> findById(UUID companyId);

    Optional<Company> findByExternalId(String externalId);

    Optional<Company> findByTenantId(UUID tenantId);

    void save(Company company);

    void saveStoreId(UUID companyId, Long storeId);
}
