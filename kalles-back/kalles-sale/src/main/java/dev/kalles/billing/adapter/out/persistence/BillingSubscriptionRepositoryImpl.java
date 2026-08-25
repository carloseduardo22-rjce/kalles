package dev.kalles.billing.adapter.out.persistence;

import dev.kalles.billing.adapter.out.persistence.entity.BillingSubscriptionEntity;
import dev.kalles.billing.adapter.out.persistence.repository.SpringDataBillingSubscriptionRepository;
import dev.kalles.billing.application.port.out.BillingSubscriptionRepository;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BillingSubscriptionRepositoryImpl implements BillingSubscriptionRepository {

    private final SpringDataBillingSubscriptionRepository repository;

    @Override
    public Optional<BillingSubscription> findByTenantIdAndProvider(UUID tenantId, BillingProvider provider) {
        return repository.findByTenantIdAndProvider(tenantId, provider).map(this::toDomain);
    }

    @Override
    public Optional<BillingSubscription> findByExternalSubscriptionId(String externalSubscriptionId) {
        return repository.findByExternalSubscriptionId(externalSubscriptionId).map(this::toDomain);
    }

    @Override
    public Optional<BillingSubscription> findByExternalCustomerId(String externalCustomerId) {
        return repository.findByExternalCustomerId(externalCustomerId).map(this::toDomain);
    }

    @Override
    public BillingSubscription save(BillingSubscription subscription) {
        return toDomain(repository.save(toEntity(subscription)));
    }

    private BillingSubscriptionEntity toEntity(BillingSubscription subscription) {
        BillingSubscriptionEntity entity = new BillingSubscriptionEntity();
        entity.setId(subscription.getId());
        entity.setTenantId(subscription.getTenantId());
        entity.setProvider(subscription.getProvider());
        entity.setExternalCustomerId(subscription.getExternalCustomerId());
        entity.setExternalSubscriptionId(subscription.getExternalSubscriptionId());
        entity.setExternalCheckoutSessionId(subscription.getExternalCheckoutSessionId());
        entity.setExternalPriceId(subscription.getExternalPriceId());
        entity.setExternalProductId(subscription.getExternalProductId());
        entity.setPlanCode(subscription.getPlanCode());
        entity.setStatus(subscription.getStatus());
        entity.setInterval(subscription.getInterval());
        entity.setCurrentPeriodStart(subscription.getCurrentPeriodStart());
        entity.setCurrentPeriodEnd(subscription.getCurrentPeriodEnd());
        entity.setCancelAtPeriodEnd(subscription.isCancelAtPeriodEnd());
        entity.setLastEventId(subscription.getLastEventId());
        return entity;
    }

    private BillingSubscription toDomain(BillingSubscriptionEntity entity) {
        return BillingSubscription.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .provider(entity.getProvider())
                .externalCustomerId(entity.getExternalCustomerId())
                .externalSubscriptionId(entity.getExternalSubscriptionId())
                .externalCheckoutSessionId(entity.getExternalCheckoutSessionId())
                .externalPriceId(entity.getExternalPriceId())
                .externalProductId(entity.getExternalProductId())
                .planCode(entity.getPlanCode())
                .status(entity.getStatus())
                .interval(entity.getInterval())
                .currentPeriodStart(entity.getCurrentPeriodStart())
                .currentPeriodEnd(entity.getCurrentPeriodEnd())
                .cancelAtPeriodEnd(entity.isCancelAtPeriodEnd())
                .lastEventId(entity.getLastEventId())
                .build();
    }
}
