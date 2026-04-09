package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.support.LegacyMercadoPagoReferenceTest;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@LegacyMercadoPagoReferenceTest
class CancelPaymentOrderUseCaseTest {

    private MercadoPagoOrderPort orderPort;
    private CancelPaymentOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderPort = mock(MercadoPagoOrderPort.class);
        useCase = new CancelPaymentOrderUseCase(orderPort);
    }

    @Test
    void shouldCancelPaymentOrder() {
        // Arrange
        String orderId = "ORD_123";

        doNothing().when(orderPort).cancelOrderPoint(orderId);

        // Act
        useCase.execute(orderId);

        // Assert
        verify(orderPort, times(1)).cancelOrderPoint(orderId);
    }
}
