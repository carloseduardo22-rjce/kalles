package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentProviderAccount;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAccountRepository {

    Optional<PaymentProviderAccount> findByTenantIdAndProvider(UUID tenantId, PaymentProvider provider);

    void save(PaymentProviderAccount account);
}
