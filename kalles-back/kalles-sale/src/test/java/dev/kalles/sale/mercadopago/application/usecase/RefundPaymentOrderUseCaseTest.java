package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.support.LegacyMercadoPagoReferenceTest;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@LegacyMercadoPagoReferenceTest
class RefundPaymentOrderUseCaseTest {

    private MercadoPagoOrderPort orderPort;
    private RefundPaymentOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderPort = mock(MercadoPagoOrderPort.class);
        useCase = new RefundPaymentOrderUseCase(orderPort);
    }

    @Test
    void shouldRefundPaymentOrder() {
        // Arrange
        String paymentId = "PAY_456";

        doNothing().when(orderPort).refundOrderPoint(paymentId);

        // Act
        useCase.execute(paymentId);

        // Assert
        verify(orderPort, times(1)).refundOrderPoint(paymentId);
    }
}
