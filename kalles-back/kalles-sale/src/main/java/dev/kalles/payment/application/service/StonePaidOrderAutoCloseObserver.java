package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.in.ClosePaymentOrderUseCase;
import dev.kalles.payment.application.port.out.PaymentWebhookEventObserver;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStatus;
import dev.kalles.payment.domain.PaymentWebhookEvent;
import org.springframework.stereotype.Component;

@Component
public class StonePaidOrderAutoCloseObserver implements PaymentWebhookEventObserver {

    private final ClosePaymentOrderUseCase closePaymentOrderUseCase;

    public StonePaidOrderAutoCloseObserver(ClosePaymentOrderUseCase closePaymentOrderUseCase) {
        this.closePaymentOrderUseCase = closePaymentOrderUseCase;
    }

    @Override
    public void onEvent(PaymentWebhookEvent event) {
        if (event.provider() != PaymentProvider.STONE) {
            return;
        }
        if (event.status() != PaymentStatus.APPROVED) {
            return;
        }
        if (event.providerOrderId() == null || event.providerOrderId().isBlank()) {
            return;
        }

        closePaymentOrderUseCase.execute(PaymentProvider.STONE, event.providerOrderId(), PaymentStatus.APPROVED);
    }
}
