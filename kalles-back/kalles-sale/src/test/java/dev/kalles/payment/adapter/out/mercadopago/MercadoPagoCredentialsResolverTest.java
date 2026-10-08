package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.application.port.out.PaymentAccountRepository;
import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.exception.PaymentProviderAccountNotLinkedException;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MercadoPagoCredentialsResolverTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.tenant(TENANT_ID);

    private final PaymentAccountRepository paymentAccountRepository = mock(PaymentAccountRepository.class);
    private final MercadoPagoCredentialsResolver resolver = new MercadoPagoCredentialsResolver(
            paymentAccountRepository,
            new MercadoPagoProperties(null, null, null, null, null, null, null)
    );

    @Test
    void shouldRejectAccessTokenRequestWhenTheTenantHasNoLinkedAccount() {
        when(paymentAccountRepository.findByTenantIdAndProvider(TENANT_ID, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(resolver::linkedAccessTokenOrThrow)
                .isInstanceOf(PaymentProviderAccountNotLinkedException.class);
    }

    @Test
    void shouldRejectUserIdRequestWhenTheTenantHasNoLinkedAccount() {
        when(paymentAccountRepository.findByTenantIdAndProvider(TENANT_ID, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(resolver::linkedUserIdOrThrow)
                .isInstanceOf(PaymentProviderAccountNotLinkedException.class);
    }
}
