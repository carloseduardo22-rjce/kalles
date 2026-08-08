package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;
import dev.kalles.shared.service.CheckoutSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentLifecycleServiceTest {

    private PaymentProviderPortFactory paymentProviderPortFactory;
    private PaymentOrderRepository paymentOrderRepository;
    private PaymentGatewayPort paymentGatewayPort;
    private CheckoutSessionService checkoutSessionService;
    private PaymentLifecycleService paymentLifecycleService;

    @BeforeEach
    void setUp() {
        paymentProviderPortFactory = mock(PaymentProviderPortFactory.class);
        paymentOrderRepository = mock(PaymentOrderRepository.class);
        paymentGatewayPort = mock(PaymentGatewayPort.class);
        checkoutSessionService = mock(CheckoutSessionService.class);
        paymentLifecycleService = new PaymentLifecycleService(
            paymentProviderPortFactory,
            paymentOrderRepository,
            checkoutSessionService
        );

        when(paymentProviderPortFactory.gateway(PaymentProvider.MERCADO_PAGO)).thenReturn(paymentGatewayPort);
        when(checkoutSessionService.findByToken(any())).thenReturn(Optional.empty());
    }

    @Test
    void shouldGenerateIdempotencyKeyAndPersistTerminalPayments() {
        PaymentCommand command = new PaymentCommand(
                PaymentProvider.MERCADO_PAGO,
                PaymentFlow.TERMINAL,
                "ERP-ORDER-1",
                new BigDecimal("79.90"),
                "TERM-001",
                null,
                "Venda no terminal",
                PaymentMethodType.CREDIT_CARD,
                Map.of("cashier", "PDV-01")
        );
        PaymentResult gatewayResult = new PaymentResult(
                "MP-ORDER-1",
                "MP-PAYMENT-1",
                PaymentStatus.APPROVED,
                Map.of("providerStatus", "approved")
        );

        when(paymentGatewayPort.processPayment(any(PaymentCommand.class))).thenReturn(gatewayResult);

        PaymentResult result = paymentLifecycleService.execute(command);

        ArgumentCaptor<PaymentCommand> commandCaptor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentGatewayPort).processPayment(commandCaptor.capture());

        PaymentCommand forwardedCommand = commandCaptor.getValue();
        assertThat(forwardedCommand.idempotencyKey()).isNotBlank();
        assertThat(forwardedCommand.externalReference()).isEqualTo("ERP-ORDER-1");
        assertThat(result).isEqualTo(gatewayResult);

        ArgumentCaptor<PaymentOrder> paymentOrderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(paymentOrderRepository).save(paymentOrderCaptor.capture());

        PaymentOrder persistedOrder = paymentOrderCaptor.getValue();
        assertThat(persistedOrder.provider()).isEqualTo(PaymentProvider.MERCADO_PAGO);
        assertThat(persistedOrder.providerOrderId()).isEqualTo("MP-ORDER-1");
        assertThat(persistedOrder.providerPaymentId()).isEqualTo("MP-PAYMENT-1");
        assertThat(persistedOrder.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(persistedOrder.flow()).isEqualTo(PaymentFlow.TERMINAL);
        assertThat(persistedOrder.methodType()).isEqualTo(PaymentMethodType.CREDIT_CARD);
        assertThat(persistedOrder.idempotencyKey()).isEqualTo(forwardedCommand.idempotencyKey());
    }

    @Test
    void shouldKeepExistingIdempotencyKeyAndSkipPersistenceForQrPayments() {
        PaymentCommand command = new PaymentCommand(
                PaymentProvider.MERCADO_PAGO,
                PaymentFlow.QR_CODE,
                "ERP-ORDER-QR-1",
                new BigDecimal("25.00"),
                "CAIXA-01",
                "idem-qr-001",
                "Cobranca QR",
                PaymentMethodType.UNSPECIFIED,
                Map.of("channel", "totem")
        );
        PaymentResult gatewayResult = new PaymentResult(
                "MP-QR-ORDER-1",
                null,
                PaymentStatus.CREATED,
                Map.of("qrData", "000201010212")
        );

        when(paymentGatewayPort.processPayment(any(PaymentCommand.class))).thenReturn(gatewayResult);

        paymentLifecycleService.execute(command);

        verify(paymentGatewayPort).processPayment(any(PaymentCommand.class));
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));

        ArgumentCaptor<PaymentCommand> commandCaptor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentGatewayPort).processPayment(commandCaptor.capture());
        assertThat(commandCaptor.getValue().idempotencyKey()).isEqualTo("idem-qr-001");
    }
}

