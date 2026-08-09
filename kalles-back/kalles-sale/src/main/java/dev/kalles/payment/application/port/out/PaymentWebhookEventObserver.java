package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentWebhookEvent;

public interface PaymentWebhookEventObserver {

    void onEvent(PaymentWebhookEvent event);
}
