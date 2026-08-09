package dev.kalles.billing.application.service;

import dev.kalles.billing.application.port.out.BillingGateway;
import dev.kalles.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBillingPortalSessionUseCase {

    private final BillingGateway billingGateway;
    private final BillingSubscriptionRepository billingSubscriptionRepository;

    public BillingGateway.PortalSession execute(UUID tenantId, String returnUrl) {
        BillingSubscription subscription = billingSubscriptionRepository
                .findByTenantIdAndProvider(tenantId, BillingProvider.STRIPE)
                .orElseThrow(() -> new IllegalStateException("Nenhuma assinatura Stripe encontrada para este tenant."));

        if (subscription.getExternalCustomerId() == null || subscription.getExternalCustomerId().isBlank()) {
            throw new IllegalStateException("Nenhum cliente Stripe encontrado para este tenant.");
        }

        return billingGateway.createPortalSession(
                new BillingGateway.PortalCommand(subscription.getExternalCustomerId(), returnUrl)
        );
    }
}
