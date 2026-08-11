package dev.kalles.fiscal.adapter.out.persistence;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalConfigurationEntity;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalConfigurationRepository;
import dev.kalles.fiscal.application.port.out.FiscalConfigurationRepository;
import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FiscalConfigurationPersistenceAdapter implements FiscalConfigurationRepository {

    private final SpringDataFiscalConfigurationRepository repository;

    @Override
    public Optional<FiscalConfiguration> findByCompany(UUID tenantId, UUID companyId, FiscalDocumentModel model, FiscalEnvironment environment) {
        return repository.findByTenantIdAndCompanyIdAndModelAndEnvironment(tenantId, companyId, model, environment)
                .map(FiscalConfigurationEntity::toDomain);
    }

    @Override
    @Transactional
    public long reserveNextNumber(UUID tenantId, UUID companyId, FiscalDocumentModel model, FiscalEnvironment environment) {
        FiscalConfigurationEntity entity = repository
                .findForNumberReservation(tenantId, companyId, model, environment)
                .orElseThrow(() -> new NotFoundException("Configuracao fiscal da filial nao encontrada"));

        long reserved = entity.getNextNumber() == null ? 1L : entity.getNextNumber();
        entity.setNextNumber(reserved + 1);
        repository.save(entity);
        return reserved;
    }

    @Override
    public FiscalConfiguration save(FiscalConfiguration configuration) {
        FiscalConfigurationEntity entity = repository
                .findByTenantIdAndCompanyIdAndModelAndEnvironment(configuration.tenantId(), configuration.companyId(),
                        configuration.model(), configuration.environment())
                .orElseGet(FiscalConfigurationEntity::new);
        entity.setTenantId(configuration.tenantId());
        entity.setCompanyId(configuration.companyId());
        entity.setModel(configuration.model());
        entity.setEnvironment(configuration.environment());
        entity.setStateCode(configuration.stateCode());
        entity.setCscId(configuration.cscId());
        entity.setCscToken(configuration.cscToken());
        entity.setSeries(configuration.series());
        entity.setNextNumber(configuration.nextNumber());
        return repository.save(entity).toDomain();
    }
}
