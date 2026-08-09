package dev.kalles.fiscal.adapter.out.persistence;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalIssuerProfileEntity;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalIssuerProfileRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerProfileRepository;
import dev.kalles.fiscal.domain.FiscalIssuerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FiscalIssuerProfilePersistenceAdapter implements FiscalIssuerProfileRepository {

    private final SpringDataFiscalIssuerProfileRepository repository;

    @Override
    public Optional<FiscalIssuerProfile> findByCompany(UUID tenantId, UUID companyId) {
        return repository.findByTenantIdAndCompanyId(tenantId, companyId)
                .map(FiscalIssuerProfileEntity::toDomain);
    }

    @Override
    public FiscalIssuerProfile save(FiscalIssuerProfile profile) {
        FiscalIssuerProfileEntity entity = repository.findByTenantIdAndCompanyId(profile.tenantId(), profile.companyId())
                .orElseGet(FiscalIssuerProfileEntity::new);
        entity.setTenantId(profile.tenantId());
        entity.setCompanyId(profile.companyId());
        entity.setCnpj(profile.cnpj());
        entity.setLegalName(profile.legalName());
        entity.setTradeName(profile.tradeName());
        entity.setStateRegistration(profile.stateRegistration());
        entity.setTaxRegime(profile.taxRegime());
        entity.setCnae(profile.cnae());
        return repository.save(entity).toDomain();
    }
}
