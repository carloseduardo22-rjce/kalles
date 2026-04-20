package dev.kalles.sale.billing.application;

import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.sale.billing.application.service.CreateBillingCheckoutSessionUseCase;
import dev.kalles.sale.billing.application.service.StripeBillingProperties;
import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import dev.kalles.sale.billing.domain.BillingSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBillingCheckoutSessionUseCaseTest {

    private BillingGateway billingGateway;
    private BillingSubscriptionRepository billingSubscriptionRepository;
    private CreateBillingCheckoutSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        billingGateway = mock(BillingGateway.class);
        billingSubscriptionRepository = mock(BillingSubscriptionRepository.class);
        useCase = new CreateBillingCheckoutSessionUseCase(
                billingGateway,
                billingSubscriptionRepository,
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
    void shouldCreateSubscriptionCheckoutWhenTenantHasNoSubscriptionYet() {
        UUID tenantId = UUID.randomUUID();
        when(billingSubscriptionRepository.findByTenantIdAndProvider(tenantId, BillingProvider.STRIPE))
                .thenReturn(Optional.empty());
        when(billingGateway.createSubscriptionCheckout(any()))
                .thenReturn(new BillingGateway.CheckoutSession(
                        "cs_123",
                        "secret_123",
                        "https://checkout.stripe.test/cs_123",
                        "cus_123",
                        "sub_123",
                        BillingStatus.CHECKOUT_CREATED
                ));
        when(billingSubscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BillingGateway.CheckoutSession result = useCase.execute(
                tenantId,
                "billing@kalles.dev",
                "Kalles Admin",
                "https://app.kalles.dev/billing/return"
        );

        assertEquals("cs_123", result.sessionId());
        verify(billingGateway).createSubscriptionCheckout(any(BillingGateway.CheckoutCommand.class));
        verify(billingSubscriptionRepository).save(any(BillingSubscription.class));
    }

    @Test
    void shouldReuseExistingStripeCustomerWhenSubscriptionAlreadyExists() {
        UUID tenantId = UUID.randomUUID();
        BillingSubscription existingSubscription = new BillingSubscription(
                UUID.randomUUID(),
                tenantId,
                BillingProvider.STRIPE,
                "cus_existing",
                "sub_existing",
                "cs_existing",
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
                .thenReturn(Optional.of(existingSubscription));
        when(billingGateway.createSubscriptionCheckout(any()))
                .thenReturn(new BillingGateway.CheckoutSession(
                        "cs_456",
                        "secret_456",
                        "https://checkout.stripe.test/cs_456",
                        "cus_existing",
                        "sub_new",
                        BillingStatus.CHECKOUT_CREATED
                ));
        when(billingSubscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BillingGateway.CheckoutSession result = useCase.execute(
                tenantId,
                "billing@kalles.dev",
                "Kalles Admin",
                "https://app.kalles.dev/billing/return"
        );

        assertEquals("cus_existing", result.customerId());
        assertEquals("cus_existing", existingSubscription.getExternalCustomerId());
        assertEquals("sub_new", existingSubscription.getExternalSubscriptionId());
        assertEquals("cs_456", existingSubscription.getExternalCheckoutSessionId());
        assertEquals(BillingStatus.CHECKOUT_CREATED, existingSubscription.getStatus());
        assertNull(existingSubscription.getCurrentPeriodStart());
    }
}
