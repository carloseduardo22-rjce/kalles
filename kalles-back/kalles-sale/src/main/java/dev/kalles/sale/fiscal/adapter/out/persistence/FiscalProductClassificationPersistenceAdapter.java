package dev.kalles.sale.fiscal.adapter.out.persistence;

import dev.kalles.sale.fiscal.adapter.out.persistence.entity.FiscalProductClassificationEntity;
import dev.kalles.sale.fiscal.adapter.out.persistence.repository.SpringDataFiscalProductClassificationRepository;
import dev.kalles.sale.fiscal.application.port.out.FiscalProductClassificationRepository;
import dev.kalles.sale.fiscal.domain.FiscalProductClassification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FiscalProductClassificationPersistenceAdapter implements FiscalProductClassificationRepository {

    private final SpringDataFiscalProductClassificationRepository repository;

    @Override
    public Optional<FiscalProductClassification> findByProduct(UUID tenantId, UUID companyId, UUID productId) {
        return repository.findByTenantIdAndCompanyIdAndProductId(tenantId, companyId, productId)
                .map(FiscalProductClassificationEntity::toDomain);
    }

    @Override
    public FiscalProductClassification save(FiscalProductClassification classification) {
        FiscalProductClassificationEntity entity = repository
                .findByTenantIdAndCompanyIdAndProductId(classification.tenantId(), classification.companyId(), classification.productId())
                .orElseGet(FiscalProductClassificationEntity::new);
        entity.setTenantId(classification.tenantId());
        entity.setCompanyId(classification.companyId());
        entity.setProductId(classification.productId());
        entity.setNcm(classification.ncm());
        entity.setCest(classification.cest());
        entity.setCfop(classification.cfop());
        entity.setCfopSale(classification.cfopSale());
        entity.setOrigin(classification.origin());
        entity.setCsosn(classification.csosn());
        entity.setCst(classification.cst());
        entity.setUnit(classification.unit());
        entity.setGtin(classification.gtin());
        return repository.save(entity).toDomain();
    }
}
