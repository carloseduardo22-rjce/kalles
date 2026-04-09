package dev.kalles.sale.payment.support;

import dev.kalles.sale.payment.application.port.out.PaymentWebhookEventObserver;
import dev.kalles.sale.payment.domain.PaymentWebhookEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StoneWebhookEventProbe implements PaymentWebhookEventObserver {

    private final AtomicReference<PaymentWebhookEvent> lastEvent = new AtomicReference<>();
    private final AtomicInteger eventCount = new AtomicInteger();

    @Override
    public void onEvent(PaymentWebhookEvent event) {
        if (event.provider() == dev.kalles.sale.payment.domain.PaymentProvider.STONE) {
            lastEvent.set(event);
            eventCount.incrementAndGet();
        }
    }

    public void reset() {
        lastEvent.set(null);
        eventCount.set(0);
    }

    public PaymentWebhookEvent lastEvent() {
        return lastEvent.get();
    }

    public int eventCount() {
        return eventCount.get();
    }
}
