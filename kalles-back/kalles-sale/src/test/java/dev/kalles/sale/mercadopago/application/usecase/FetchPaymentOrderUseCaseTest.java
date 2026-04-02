package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.ResultadoPoint;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class FetchPaymentOrderUseCaseTest {

    private MercadoPagoOrderPort orderPort;
    private FetchPaymentOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderPort = mock(MercadoPagoOrderPort.class);
        useCase = new FetchPaymentOrderUseCase(orderPort);
    }

    @Test
    void shouldFetchPaymentOrder() {
        // Arrange
        String orderId = "ORD_789";
        ResultadoPoint expectedResult = new ResultadoPoint(orderId, "paid", "PAY_789");

        when(orderPort.getOrderPoint(orderId)).thenReturn(expectedResult);

        // Act
        ResultadoPoint result = useCase.execute(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.orderId());
        assertEquals("paid", result.status());
        assertEquals("PAY_789", result.paymentId());

        verify(orderPort, times(1)).getOrderPoint(orderId);
    }
}
