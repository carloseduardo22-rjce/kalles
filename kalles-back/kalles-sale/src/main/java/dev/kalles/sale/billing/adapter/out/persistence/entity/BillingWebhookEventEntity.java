package dev.kalles.sale.billing.adapter.out.persistence.entity;

import dev.kalles.sale.billing.domain.BillingProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "billing_webhook_event")
@Getter
@Setter
public class BillingWebhookEventEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillingProvider provider;

    @Column(name = "external_event_id", nullable = false, unique = true, length = 255)
    private String externalEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
}
