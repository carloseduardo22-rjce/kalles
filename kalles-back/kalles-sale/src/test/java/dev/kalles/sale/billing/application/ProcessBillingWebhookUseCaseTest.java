package dev.kalles.sale.billing.application;

import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.sale.billing.application.port.out.BillingWebhookEventRepository;
import dev.kalles.sale.billing.application.service.ProcessBillingWebhookUseCase;
import dev.kalles.sale.billing.application.service.StripeBillingProperties;
import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import dev.kalles.sale.billing.domain.BillingSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessBillingWebhookUseCaseTest {

    private BillingGateway billingGateway;
    private BillingSubscriptionRepository billingSubscriptionRepository;
    private BillingWebhookEventRepository billingWebhookEventRepository;
    private ProcessBillingWebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        billingGateway = mock(BillingGateway.class);
        billingSubscriptionRepository = mock(BillingSubscriptionRepository.class);
        billingWebhookEventRepository = mock(BillingWebhookEventRepository.class);

        useCase = new ProcessBillingWebhookUseCase(
                billingGateway,
                billingSubscriptionRepository,
                billingWebhookEventRepository,
                new StripeBillingProperties(
                        "sk_test",
                        "pk_test",
                        "whsec_test",
                        "price_monthly",
                        "bpc_123",
                        "default-monthly"
                )
        );
    }

    @Test
    void shouldUpsertSubscriptionStateFromSubscriptionWebhook() {
        UUID tenantId = UUID.randomUUID();
        Instant currentPeriodStart = Instant.parse("2026-04-01T00:00:00Z");
        Instant currentPeriodEnd = Instant.parse("2026-05-01T00:00:00Z");

        when(billingGateway.parseWebhook("{payload}", "signature"))
                .thenReturn(new BillingGateway.WebhookNotification(
                        "evt_123",
                        "customer.subscription.updated",
                        BillingProvider.STRIPE,
                        tenantId,
                        "cus_123",
                        "sub_123",
                        null,
                        "prod_monthly",
                        "price_monthly",
                        BillingStatus.ACTIVE,
                        BillingInterval.MONTHLY,
                        currentPeriodStart,
                        currentPeriodEnd,
                        false
                ));
        when(billingWebhookEventRepository.existsByProviderAndExternalEventId(BillingProvider.STRIPE, "evt_123"))
                .thenReturn(false);
        when(billingSubscriptionRepository.findByExternalSubscriptionId("sub_123")).thenReturn(Optional.empty());
        when(billingSubscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("{payload}", "signature");

        verify(billingSubscriptionRepository).save(any(BillingSubscription.class));
        verify(billingWebhookEventRepository).save(any());
    }

    @Test
    void shouldIgnorePreviouslyProcessedWebhookEvent() {
        when(billingGateway.parseWebhook("{payload}", "signature"))
                .thenReturn(new BillingGateway.WebhookNotification(
                        "evt_123",
                        "customer.subscription.updated",
                        BillingProvider.STRIPE,
                        UUID.randomUUID(),
                        "cus_123",
                        "sub_123",
                        null,
                        "prod_monthly",
                        "price_monthly",
                        BillingStatus.ACTIVE,
                        BillingInterval.MONTHLY,
                        Instant.now(),
                        Instant.now(),
                        false
                ));
        when(billingWebhookEventRepository.existsByProviderAndExternalEventId(BillingProvider.STRIPE, "evt_123"))
                .thenReturn(true);

        useCase.execute("{payload}", "signature");

        verify(billingSubscriptionRepository, never()).save(any());
        verify(billingWebhookEventRepository, never()).save(any());
    }

    @Test
    void shouldResolveTenantUsingExistingCustomerWhenWebhookHasNoMetadata() {
        UUID tenantId = UUID.randomUUID();
        BillingSubscription existingSubscription = new BillingSubscription(
                UUID.randomUUID(),
                tenantId,
                BillingProvider.STRIPE,
                "cus_123",
                "sub_123",
                "cs_123",
                "price_monthly",
                "prod_monthly",
                "default-monthly",
                BillingStatus.INCOMPLETE,
                BillingInterval.MONTHLY,
                null,
                null,
                false,
                null
        );

        when(billingGateway.parseWebhook("{payload}", "signature"))
                .thenReturn(new BillingGateway.WebhookNotification(
                        "evt_124",
                        "invoice.payment_failed",
                        BillingProvider.STRIPE,
                        null,
                        "cus_123",
                        "sub_123",
                        null,
                        "prod_monthly",
                        "price_monthly",
                        BillingStatus.PAST_DUE,
                        BillingInterval.MONTHLY,
                        Instant.parse("2026-04-01T00:00:00Z"),
                        Instant.parse("2026-05-01T00:00:00Z"),
                        false
                ));
        when(billingWebhookEventRepository.existsByProviderAndExternalEventId(BillingProvider.STRIPE, "evt_124"))
                .thenReturn(false);
        when(billingSubscriptionRepository.findByExternalSubscriptionId("sub_123")).thenReturn(Optional.of(existingSubscription));
        when(billingSubscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("{payload}", "signature");

        verify(billingSubscriptionRepository).save(any(BillingSubscription.class));
    }
}
