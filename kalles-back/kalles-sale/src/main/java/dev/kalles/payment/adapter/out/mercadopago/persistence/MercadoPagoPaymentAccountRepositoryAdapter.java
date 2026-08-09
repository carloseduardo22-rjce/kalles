package dev.kalles.payment.adapter.out.mercadopago.persistence;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoTenantAccountEntity;
import dev.kalles.payment.adapter.out.mercadopago.persistence.repository.MercadoPagoTenantAccountJpaRepository;
import dev.kalles.payment.application.port.out.PaymentAccountRepository;
import dev.kalles.payment.application.service.PaymentProviderCredentialCipherService;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentProviderAccount;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MercadoPagoPaymentAccountRepositoryAdapter implements PaymentAccountRepository {

    private final MercadoPagoTenantAccountJpaRepository repository;
    private final PaymentProviderCredentialCipherService cipherService;

    public MercadoPagoPaymentAccountRepositoryAdapter(
            MercadoPagoTenantAccountJpaRepository repository,
            PaymentProviderCredentialCipherService cipherService
    ) {
        this.repository = repository;
        this.cipherService = cipherService;
    }

    @Override
    public Optional<PaymentProviderAccount> findByTenantIdAndProvider(UUID tenantId, PaymentProvider provider) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            return Optional.empty();
        }

        return repository.findById(tenantId).map(this::toDomain);
    }

    @Override
    public void save(PaymentProviderAccount account) {
        if (account.provider() != PaymentProvider.MERCADO_PAGO) {
            throw new UnsupportedOperationException("This repository only supports Mercado Pago accounts");
        }

        MercadoPagoTenantAccountEntity entity = repository.findById(account.tenantId())
                .orElse(new MercadoPagoTenantAccountEntity());
        entity.setTenantId(account.tenantId());
        entity.setAccessToken(cipherService.encrypt(account.accessToken()));
        entity.setRefreshToken(cipherService.encrypt(account.refreshToken()));
        entity.setProviderAccountId(cipherService.encrypt(account.providerAccountId()));
        repository.save(entity);
    }

    private PaymentProviderAccount toDomain(MercadoPagoTenantAccountEntity entity) {
        return new PaymentProviderAccount(
                entity.getTenantId(),
                PaymentProvider.MERCADO_PAGO,
                cipherService.decrypt(entity.getProviderAccountId()),
                cipherService.decrypt(entity.getAccessToken()),
                cipherService.decrypt(entity.getRefreshToken()),
                null,
                null,
                null,
                null,
                null
        );
    }
}
