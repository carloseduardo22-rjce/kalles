package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClosePaymentOrderServiceTest {

    private PaymentProviderPortFactory paymentProviderPortFactory;
    private PaymentOrderRepository paymentOrderRepository;
    private PaymentGatewayPort paymentGatewayPort;
    private ClosePaymentOrderService service;

    @BeforeEach
    void setUp() {
        paymentProviderPortFactory = mock(PaymentProviderPortFactory.class);
        paymentOrderRepository = mock(PaymentOrderRepository.class);
        paymentGatewayPort = mock(PaymentGatewayPort.class);
        service = new ClosePaymentOrderService(paymentProviderPortFactory, paymentOrderRepository);

        when(paymentProviderPortFactory.gateway(PaymentProvider.MERCADO_PAGO)).thenReturn(paymentGatewayPort);
    }

    @Test
    void shouldCloseOrderAndPersistReturnedStatus() {
        PaymentOrder existingOrder = new PaymentOrder(
                PaymentProvider.MERCADO_PAGO,
                "or_mp_1",
                null,
                PaymentStatus.PENDING,
                "ERP-SALE-1001",
                new BigDecimal("125.00"),
                "idem-close",
                PaymentFlow.TERMINAL,
                PaymentMethodType.CREDIT_CARD
        );

        when(paymentOrderRepository.findByProviderOrderIdAndProvider("or_mp_1", PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(existingOrder));
        when(paymentGatewayPort.closePaymentOrder("or_mp_1", PaymentStatus.APPROVED))
                .thenReturn(new PaymentResult("or_mp_1", "ch_mp_1", PaymentStatus.APPROVED, Map.of()));

        service.execute(PaymentProvider.MERCADO_PAGO, "or_mp_1", PaymentStatus.APPROVED);

        verify(paymentGatewayPort).closePaymentOrder("or_mp_1", PaymentStatus.APPROVED);
        verify(paymentOrderRepository).save(existingOrder.withStatus(PaymentStatus.APPROVED).withProviderPaymentId("ch_mp_1"));
    }
}
