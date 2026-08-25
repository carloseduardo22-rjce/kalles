package dev.kalles.billing.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class BillingSubscription {

    private UUID id;
    private UUID tenantId;
    private BillingProvider provider;
    private String externalCustomerId;
    private String externalSubscriptionId;
    private String externalCheckoutSessionId;
    private String externalPriceId;
    private String externalProductId;
    private String planCode;
    private BillingStatus status;
    private BillingInterval interval;
    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private boolean cancelAtPeriodEnd;
    private String lastEventId;
}
