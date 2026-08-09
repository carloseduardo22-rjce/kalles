package dev.kalles.payment.adapter.out.persistence;

import dev.kalles.payment.adapter.out.persistence.entity.PaymentTerminalMappingEntity;
import dev.kalles.payment.adapter.out.persistence.repository.PaymentTerminalMappingJpaRepository;
import dev.kalles.payment.application.port.out.PaymentTerminalMappingRepository;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentTerminalMapping;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentTerminalMappingRepositoryAdapter implements PaymentTerminalMappingRepository {

    private final PaymentTerminalMappingJpaRepository repository;

    public PaymentTerminalMappingRepositoryAdapter(PaymentTerminalMappingJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentTerminalMapping> findActiveByCashRegisterIdAndProvider(UUID cashRegisterId, PaymentProvider provider) {
        return repository.findByCashRegisterIdAndProviderAndActiveTrue(cashRegisterId, provider).map(this::toDomain);
    }

    @Override
    public Optional<PaymentTerminalMapping> findActiveByCompanyIdAndProviderAndTerminalSerial(
            UUID companyId,
            PaymentProvider provider,
            String terminalSerial
    ) {
        return repository.findByCompanyIdAndProviderAndTerminalSerialAndActiveTrue(
                companyId,
                provider,
                PaymentTerminalMapping.normalizeSerial(terminalSerial)
        ).map(this::toDomain);
    }

    @Override
    public List<PaymentTerminalMapping> findActiveByCompanyIdAndProvider(UUID companyId, PaymentProvider provider) {
        return repository.findAllByCompanyIdAndProviderAndActiveTrueOrderByUpdatedAtDesc(companyId, provider)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PaymentTerminalMapping save(PaymentTerminalMapping mapping) {
        PaymentTerminalMappingEntity entity = mapping.id() == null
                ? new PaymentTerminalMappingEntity()
                : repository.findById(mapping.id()).orElse(new PaymentTerminalMappingEntity());
        entity.setTenantId(mapping.tenantId());
        entity.setCompanyId(mapping.companyId());
        entity.setCashRegisterId(mapping.cashRegisterId());
        entity.setProvider(mapping.provider());
        entity.setTerminalSerial(mapping.terminalSerial());
        entity.setActive(mapping.active());
        entity.setCreatedAt(mapping.createdAt());
        entity.setUpdatedAt(mapping.updatedAt());
        return toDomain(repository.save(entity));
    }

    private PaymentTerminalMapping toDomain(PaymentTerminalMappingEntity entity) {
        return new PaymentTerminalMapping(
                entity.getId(),
                entity.getTenantId(),
                entity.getCompanyId(),
                entity.getCashRegisterId(),
                entity.getProvider(),
                entity.getTerminalSerial(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
