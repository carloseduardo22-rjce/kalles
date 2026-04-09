package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentWebhookEvent;

public interface PaymentWebhookEventObserver {

    void onEvent(PaymentWebhookEvent event);
}
