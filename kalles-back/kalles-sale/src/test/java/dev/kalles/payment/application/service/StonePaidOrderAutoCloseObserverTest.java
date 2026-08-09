package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.in.ClosePaymentOrderUseCase;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStatus;
import dev.kalles.payment.domain.PaymentWebhookEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Tag("unit")
class StonePaidOrderAutoCloseObserverTest {

    private ClosePaymentOrderUseCase closePaymentOrderUseCase;
    private StonePaidOrderAutoCloseObserver observer;

    @BeforeEach
    void setUp() {
        closePaymentOrderUseCase = mock(ClosePaymentOrderUseCase.class);
        observer = new StonePaidOrderAutoCloseObserver(closePaymentOrderUseCase);
    }

    @Test
    void shouldCloseStoneOrderAfterApprovedWebhook() {
        observer.onEvent(new PaymentWebhookEvent(
                PaymentProvider.STONE,
                "charge.paid",
                "or_stone_123",
                "ch_stone_123",
                "sale-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.APPROVED,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        ));

        verify(closePaymentOrderUseCase).execute(PaymentProvider.STONE, "or_stone_123", PaymentStatus.APPROVED);
    }

    @Test
    void shouldIgnoreNonApprovedStoneWebhook() {
        observer.onEvent(new PaymentWebhookEvent(
                PaymentProvider.STONE,
                "charge.refunded",
                "or_stone_123",
                "ch_stone_123",
                "sale-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.REFUNDED,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        ));

        verify(closePaymentOrderUseCase, never()).execute(PaymentProvider.STONE, "or_stone_123", PaymentStatus.APPROVED);
    }
}
