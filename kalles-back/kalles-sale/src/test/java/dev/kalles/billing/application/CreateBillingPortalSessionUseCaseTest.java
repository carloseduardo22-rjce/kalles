package dev.kalles.billing.application;

import dev.kalles.billing.application.port.out.BillingGateway;
import dev.kalles.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.billing.application.service.CreateBillingPortalSessionUseCase;
import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingStatus;
import dev.kalles.billing.domain.BillingSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBillingPortalSessionUseCaseTest {

    private BillingGateway billingGateway;
    private BillingSubscriptionRepository billingSubscriptionRepository;
    private CreateBillingPortalSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        billingGateway = mock(BillingGateway.class);
        billingSubscriptionRepository = mock(BillingSubscriptionRepository.class);
        useCase = new CreateBillingPortalSessionUseCase(billingGateway, billingSubscriptionRepository);
    }

    @Test
    void shouldCreatePortalSessionWhenTenantHasStripeCustomer() {
        UUID tenantId = UUID.randomUUID();
        BillingSubscription subscription = new BillingSubscription(
                UUID.randomUUID(),
                tenantId,
                BillingProvider.STRIPE,
                "cus_123",
                "sub_123",
                "cs_123",
                "price_monthly",
                "prod_monthly",
                "default-monthly",
                BillingStatus.ACTIVE,
                BillingInterval.MONTHLY,
                null,
                null,
                false,
                null
        );
        when(billingSubscriptionRepository.findByTenantIdAndProvider(tenantId, BillingProvider.STRIPE))
                .thenReturn(Optional.of(subscription));
        when(billingGateway.createPortalSession(any()))
                .thenReturn(new BillingGateway.PortalSession("https://billing.stripe.com/session"));

        BillingGateway.PortalSession result = useCase.execute(tenantId, "https://app.kalles.dev/account");

        assertEquals("https://billing.stripe.com/session", result.url());
        verify(billingGateway).createPortalSession(any(BillingGateway.PortalCommand.class));
    }

    @Test
    void shouldRejectPortalSessionWhenTenantHasNoSubscription() {
        UUID tenantId = UUID.randomUUID();
        when(billingSubscriptionRepository.findByTenantIdAndProvider(tenantId, BillingProvider.STRIPE))
                .thenReturn(Optional.empty());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> useCase.execute(tenantId, "https://app.kalles.dev/account"));

        assertEquals("Nenhuma assinatura Stripe encontrada para este tenant.", error.getMessage());
    }

    @Test
    void shouldRejectPortalSessionWhenStripeCustomerIsMissing() {
        UUID tenantId = UUID.randomUUID();
        BillingSubscription subscription = new BillingSubscription(
                UUID.randomUUID(),
                tenantId,
                BillingProvider.STRIPE,
                null,
                "sub_123",
                "cs_123",
                "price_monthly",
                "prod_monthly",
                "default-monthly",
                BillingStatus.ACTIVE,
                BillingInterval.MONTHLY,
                null,
                null,
                false,
                null
        );
        when(billingSubscriptionRepository.findByTenantIdAndProvider(tenantId, BillingProvider.STRIPE))
                .thenReturn(Optional.of(subscription));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> useCase.execute(tenantId, "https://app.kalles.dev/account"));

        assertEquals("Nenhum cliente Stripe encontrado para este tenant.", error.getMessage());
    }
}
