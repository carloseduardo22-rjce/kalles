package dev.kalles.billing.application.service;

import dev.kalles.billing.application.port.out.BillingGateway;
import dev.kalles.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingStatus;
import dev.kalles.billing.domain.BillingSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBillingCheckoutSessionUseCase {

    private final BillingGateway billingGateway;
    private final BillingSubscriptionRepository billingSubscriptionRepository;
    private final StripeBillingProperties stripeBillingProperties;

    public BillingGateway.CheckoutSession execute(UUID tenantId, String customerEmail, String customerName, String returnUrl) {
        BillingSubscription currentSubscription = billingSubscriptionRepository
                .findByTenantIdAndProvider(tenantId, BillingProvider.STRIPE)
                .orElseGet(() -> new BillingSubscription(
                        UUID.randomUUID(),
                        tenantId,
                        BillingProvider.STRIPE,
                        null,
                        null,
                        null,
                        stripeBillingProperties.monthlyPriceId(),
                        null,
                        stripeBillingProperties.defaultPlanCode(),
                        BillingStatus.CHECKOUT_CREATED,
                        BillingInterval.MONTHLY,
                        null,
                        null,
                        false,
                        null
                ));

        BillingGateway.CheckoutSession checkoutSession = billingGateway.createSubscriptionCheckout(
                new BillingGateway.CheckoutCommand(
                        tenantId,
                        currentSubscription.getExternalCustomerId(),
                        customerEmail,
                        customerName,
                        stripeBillingProperties.monthlyPriceId(),
                        returnUrl
                )
        );

        currentSubscription.setExternalCustomerId(checkoutSession.customerId());
        currentSubscription.setExternalSubscriptionId(checkoutSession.subscriptionId());
        currentSubscription.setExternalCheckoutSessionId(checkoutSession.sessionId());
        currentSubscription.setExternalPriceId(stripeBillingProperties.monthlyPriceId());
        currentSubscription.setPlanCode(stripeBillingProperties.defaultPlanCode());
        currentSubscription.setInterval(BillingInterval.MONTHLY);
        currentSubscription.setStatus(checkoutSession.status());

        billingSubscriptionRepository.save(currentSubscription);

        return checkoutSession;
    }
}
