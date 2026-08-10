package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.application.port.out.PaymentWebhookEventObserver;
import dev.kalles.payment.application.port.out.PaymentWebhookPort;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStatus;
import dev.kalles.payment.domain.PaymentWebhookEvent;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class ProcessPaymentWebhookServiceTest {

    private PaymentProviderPortFactory portFactory;
    private PaymentWebhookPort webhookPort;
    private PaymentOrderRepository paymentOrderRepository;
    private PaymentService paymentService;
    private PaymentWebhookEventObserver observer;
    private ProcessPaymentWebhookService service;

    @BeforeEach
    void setUp() {
        portFactory = mock(PaymentProviderPortFactory.class);
        webhookPort = mock(PaymentWebhookPort.class);
        paymentOrderRepository = mock(PaymentOrderRepository.class);
        paymentService = mock(PaymentService.class);
        observer = mock(PaymentWebhookEventObserver.class);
        service = new ProcessPaymentWebhookService(
                portFactory,
                paymentOrderRepository,
                paymentService,
                List.of(observer)
        );

        when(portFactory.webhook(PaymentProvider.MERCADO_PAGO)).thenReturn(webhookPort);
    }

    @Test
    void shouldRegisterSalePaymentOnlyAfterApprovedWebhook() {
        PaymentWebhookEvent event = new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                "charge.paid",
                "or_mp_123",
                "ch_mp_123",
                "session-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.APPROVED,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        );
        PaymentOrder existingOrder = existingOrder(PaymentStatus.PENDING);

        when(webhookPort.parseEvent(Map.of("type", "charge.paid"))).thenReturn(event);
        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder));

        boolean processed = service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.paid"));

        assertThat(processed).isTrue();
        verify(paymentService).registerExternalPayment(
                "session-token-1", PaymentMethod.CREDIT_CARD, new BigDecimal("125.00"), "ch_mp_123");
        verify(observer).onEvent(event);

        ArgumentCaptor<PaymentOrder> orderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(paymentOrderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(orderCaptor.getValue().providerPaymentId()).isEqualTo("ch_mp_123");
    }

    @Test
    void shouldNotRegisterSalePaymentAgainWhenApprovedWebhookIsResent() {
        PaymentWebhookEvent event = new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                "charge.paid",
                "or_mp_123",
                "ch_mp_123",
                "session-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.APPROVED,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        );

        when(webhookPort.parseEvent(Map.of("type", "charge.paid"))).thenReturn(event);
        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder(PaymentStatus.APPROVED)));

        boolean processed = service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.paid"));

        assertThat(processed).isTrue();
        verify(paymentService, never()).registerExternalPayment(any(), any(), any(), any());
        verify(observer).onEvent(event);
    }

    @Test
    void shouldPropagateFailureWhenRegisteringSalePaymentFails() {
        PaymentWebhookEvent event = new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                "charge.paid",
                "or_mp_123",
                "ch_mp_123",
                "session-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.APPROVED,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        );

        when(webhookPort.parseEvent(Map.of("type", "charge.paid"))).thenReturn(event);
        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder(PaymentStatus.PENDING)));
        when(paymentService.registerExternalPayment(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("sessao fechada"));

        assertThatThrownBy(() -> service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.paid")))
                .isInstanceOf(IllegalStateException.class);

        verify(observer, never()).onEvent(event);
    }

    @Test
    void shouldMapUnspecifiedMethodTypeToOther() {
        PaymentWebhookEvent event = new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                "charge.paid",
                "or_mp_123",
                "ch_mp_123",
                "session-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.APPROVED,
                PaymentMethodType.UNSPECIFIED,
                Map.of()
        );

        when(webhookPort.parseEvent(Map.of("type", "charge.paid"))).thenReturn(event);
        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder(PaymentStatus.PENDING)));

        service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.paid"));

        verify(paymentService).registerExternalPayment(
                "session-token-1", PaymentMethod.OTHER, new BigDecimal("125.00"), "ch_mp_123");
    }

    @Test
    void shouldNotRegisterSalePaymentWhenWebhookIsNotApproved() {
        PaymentWebhookEvent event = new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                "charge.created",
                "or_mp_123",
                "ch_mp_123",
                "session-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.PENDING,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        );

        when(webhookPort.parseEvent(Map.of("type", "charge.created"))).thenReturn(event);
        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder(PaymentStatus.PENDING)));

        boolean processed = service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.created"));

        assertThat(processed).isTrue();
        verify(paymentService, never()).registerExternalPayment(any(), any(), any(), any());
        verify(observer).onEvent(event);
    }

    @Test
    void shouldNotRegisterSalePaymentForRefundWebhook() {
        PaymentWebhookEvent event = new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                "charge.refunded",
                "or_mp_123",
                "ch_mp_123",
                "session-token-1",
                new BigDecimal("125.00"),
                PaymentStatus.REFUNDED,
                PaymentMethodType.CREDIT_CARD,
                Map.of()
        );

        when(webhookPort.parseEvent(Map.of("type", "charge.refunded"))).thenReturn(event);
        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder(PaymentStatus.APPROVED)));

        boolean processed = service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.refunded"));

        assertThat(processed).isTrue();
        verify(paymentService, never()).registerExternalPayment(any(), any(), any(), any());
        verify(observer).onEvent(event);
    }

    @Test
    void shouldIgnoreUnsupportedWebhookPayload() {
        when(webhookPort.parseEvent(Map.of("type", "charge.unknown"))).thenReturn(null);

        boolean processed = service.execute(PaymentProvider.MERCADO_PAGO, Map.of("type", "charge.unknown"));

        assertThat(processed).isFalse();
        verify(paymentOrderRepository, never()).findByProviderOrderIdAndProvider("or_mp_123", PaymentProvider.MERCADO_PAGO);
        verify(paymentService, never()).registerExternalPayment(any(), any(), any(), any());
        verify(observer, never()).onEvent(null);
    }

    private PaymentOrder existingOrder(PaymentStatus status) {
        return new PaymentOrder(
                PaymentProvider.MERCADO_PAGO,
                "or_mp_123",
                null,
                status,
                "session-token-1",
                new BigDecimal("125.00"),
                "idem-1",
                PaymentFlow.TERMINAL,
                PaymentMethodType.CREDIT_CARD
        );
    }
}
