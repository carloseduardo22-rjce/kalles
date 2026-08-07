package dev.kalles.fiscal.adapter.out.core;

import dev.kalles.core.repository.CompanyRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.fiscal.application.port.out.FiscalCompanyAccessPort;
import dev.kalles.fiscal.application.port.out.FiscalProductAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FiscalCoreAccessAdapter implements FiscalCompanyAccessPort, FiscalProductAccessPort {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;

    @Override
    public boolean existsByTenant(UUID tenantId, UUID companyId) {
        return companyRepository.existsByIdAndTenantId(companyId, tenantId);
    }

    @Override
    public boolean existsProductByTenant(UUID tenantId, UUID productId) {
        return productRepository.findByIdAndTenantId(productId, tenantId).isPresent();
    }
}
