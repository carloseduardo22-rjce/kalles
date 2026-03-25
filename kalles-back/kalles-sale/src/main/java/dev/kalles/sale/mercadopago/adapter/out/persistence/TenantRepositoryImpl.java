package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.TenantEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataTenantRepository;
import dev.kalles.sale.mercadopago.domain.Tenant;
import dev.kalles.sale.mercadopago.port.TenantRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TenantRepositoryImpl implements TenantRepository {

    private final SpringDataTenantRepository repository;

    public TenantRepositoryImpl(SpringDataTenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Tenant> findById(UUID tenantId) {
        return repository.findById(tenantId).map(this::toDomain);
    }

    private Tenant toDomain(TenantEntity entity) {
        return new Tenant(
                entity.getId(),
                entity.getName(),
                entity.getMpAccessToken(),
                entity.getMpRefreshToken(),
                entity.getMpUserId()
        );
    }

    @Override
    public void save(Tenant tenant) {
        TenantEntity entity = repository.findById(tenant.id()).orElse(new TenantEntity());
        entity.setId(tenant.id());
        entity.setName(tenant.name());
        entity.setMpAccessToken(tenant.mpAccessToken());
        entity.setMpRefreshToken(tenant.mpRefreshToken());
        entity.setMpUserId(tenant.mpUserId());

        repository.save(entity);
    }
}
