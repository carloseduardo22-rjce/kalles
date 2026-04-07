package dev.kalles.sale.billing.application.service;

import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.sale.billing.application.port.out.BillingWebhookEventRepository;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import dev.kalles.sale.billing.domain.BillingSubscription;
import dev.kalles.sale.billing.domain.BillingWebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ProcessBillingWebhookUseCase {

    private final BillingGateway billingGateway;
    private final BillingSubscriptionRepository billingSubscriptionRepository;
    private final BillingWebhookEventRepository billingWebhookEventRepository;
    private final StripeBillingProperties stripeBillingProperties;

    @Transactional
    public void execute(String payload, String signature) {
        BillingGateway.WebhookNotification notification = billingGateway.parseWebhook(payload, signature);

        if (billingWebhookEventRepository.existsByProviderAndExternalEventId(notification.provider(), notification.eventId())) {
            return;
        }

        if (!shouldPersist(notification)) {
            billingWebhookEventRepository.save(
                    new BillingWebhookEvent(
                            UUID.randomUUID(),
                            notification.provider(),
                            notification.eventId(),
                            notification.eventType()
                    )
            );
            return;
        }

        Optional<BillingSubscription> existingSubscription = findExistingSubscription(notification);
        UUID tenantId = resolveTenantId(notification, existingSubscription);

        BillingSubscription subscription = existingSubscription.orElseGet(() -> new BillingSubscription(
                UUID.randomUUID(),
                tenantId,
                notification.provider(),
                null,
                null,
                null,
                notification.priceId(),
                notification.productId(),
                stripeBillingProperties.defaultPlanCode(),
                BillingStatus.INCOMPLETE,
                notification.interval(),
                null,
                null,
                false,
                null
        ));

        subscription.setTenantId(tenantId);
        subscription.setProvider(notification.provider());
        updateIfPresent(notification.customerId(), subscription::setExternalCustomerId);
        updateIfPresent(notification.subscriptionId(), subscription::setExternalSubscriptionId);
        updateIfPresent(notification.checkoutSessionId(), subscription::setExternalCheckoutSessionId);
        updateIfPresent(notification.priceId(), subscription::setExternalPriceId);
        updateIfPresent(notification.productId(), subscription::setExternalProductId);

        if (notification.status() != null) {
            subscription.setStatus(notification.status());
        }

        if (notification.interval() != null) {
            subscription.setInterval(notification.interval());
        }

        subscription.setCurrentPeriodStart(notification.currentPeriodStart());
        subscription.setCurrentPeriodEnd(notification.currentPeriodEnd());
        subscription.setCancelAtPeriodEnd(notification.cancelAtPeriodEnd());
        subscription.setLastEventId(notification.eventId());
        subscription.setPlanCode(stripeBillingProperties.defaultPlanCode());

        billingSubscriptionRepository.save(subscription);

        billingWebhookEventRepository.save(
                new BillingWebhookEvent(
                        UUID.randomUUID(),
                        notification.provider(),
                        notification.eventId(),
                        notification.eventType()
                )
        );
    }

    private Optional<BillingSubscription> findExistingSubscription(BillingGateway.WebhookNotification notification) {
        if (notification.subscriptionId() != null && !notification.subscriptionId().isBlank()) {
            return billingSubscriptionRepository.findByExternalSubscriptionId(notification.subscriptionId());
        }

        if (notification.customerId() != null && !notification.customerId().isBlank()) {
            return billingSubscriptionRepository.findByExternalCustomerId(notification.customerId());
        }

        if (notification.tenantId() != null) {
            return billingSubscriptionRepository.findByTenantIdAndProvider(notification.tenantId(), BillingProvider.STRIPE);
        }

        return Optional.empty();
    }

    private UUID resolveTenantId(BillingGateway.WebhookNotification notification, Optional<BillingSubscription> existingSubscription) {
        if (notification.tenantId() != null) {
            return notification.tenantId();
        }

        return existingSubscription
                .map(BillingSubscription::getTenantId)
                .orElseThrow(() -> new IllegalStateException("Nao foi possivel resolver o tenant da notificacao Stripe."));
    }

    private boolean shouldPersist(BillingGateway.WebhookNotification notification) {
        return hasText(notification.customerId())
                || hasText(notification.subscriptionId())
                || hasText(notification.checkoutSessionId());
    }

    private void updateIfPresent(String value, Consumer<String> consumer) {
        if (hasText(value)) {
            consumer.accept(value);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
