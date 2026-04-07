package dev.kalles.sale.billing.adapter.out.persistence.entity;

import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "billing_subscription")
@Getter
@Setter
public class BillingSubscriptionEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillingProvider provider;

    @Column(name = "external_customer_id", length = 255)
    private String externalCustomerId;

    @Column(name = "external_subscription_id", length = 255)
    private String externalSubscriptionId;

    @Column(name = "external_checkout_session_id", length = 255)
    private String externalCheckoutSessionId;

    @Column(name = "external_price_id", length = 255)
    private String externalPriceId;

    @Column(name = "external_product_id", length = 255)
    private String externalProductId;

    @Column(name = "plan_code", nullable = false, length = 100)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BillingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 30)
    private BillingInterval interval;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @Column(name = "cancel_at_period_end", nullable = false)
    private boolean cancelAtPeriodEnd;

    @Column(name = "last_event_id", length = 255)
    private String lastEventId;
}
