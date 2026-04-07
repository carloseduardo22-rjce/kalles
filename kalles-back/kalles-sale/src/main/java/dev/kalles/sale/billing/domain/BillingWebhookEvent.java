package dev.kalles.sale.billing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingWebhookEvent {

    private UUID id;
    private BillingProvider provider;
    private String externalEventId;
    private String eventType;
}
