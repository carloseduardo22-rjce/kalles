package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoTenantConfigEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataTenantRepository;
import dev.kalles.sale.mercadopago.application.service.TenantCredentialCipherService;
import dev.kalles.sale.mercadopago.domain.Tenant;
import dev.kalles.sale.mercadopago.port.TenantRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TenantRepositoryImpl implements TenantRepository {

    private final SpringDataTenantRepository repository;
    private final TenantCredentialCipherService cipherService;

    public TenantRepositoryImpl(
            SpringDataTenantRepository repository,
            TenantCredentialCipherService cipherService) {
        this.repository = repository;
        this.cipherService = cipherService;
    }

    @Override
    public Optional<Tenant> findById(UUID tenantId) {
        return repository.findById(tenantId).map(this::toDomain);
    }

    private Tenant toDomain(MercadoPagoTenantConfigEntity entity) {
        return new Tenant(
                entity.getTenantId(),
                null, // No nome
                cipherService.decrypt(entity.getMpAccessToken()),
                cipherService.decrypt(entity.getMpRefreshToken()),
                cipherService.decrypt(entity.getMpUserId())
        );
    }

    @Override
    public void save(Tenant tenant) {
        MercadoPagoTenantConfigEntity entity = repository.findById(tenant.id()).orElse(new MercadoPagoTenantConfigEntity());
        entity.setTenantId(tenant.id());
        entity.setMpAccessToken(cipherService.encrypt(tenant.mpAccessToken()));
        entity.setMpRefreshToken(cipherService.encrypt(tenant.mpRefreshToken()));
        entity.setMpUserId(cipherService.encrypt(tenant.mpUserId()));

        repository.save(entity);
    }
}
