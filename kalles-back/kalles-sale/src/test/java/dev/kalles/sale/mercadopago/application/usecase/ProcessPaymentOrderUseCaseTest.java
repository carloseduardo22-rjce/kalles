package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.CobrancaPoint;
import dev.kalles.sale.mercadopago.domain.ResultadoPoint;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import dev.kalles.sale.mercadopago.port.PointOrderPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessPaymentOrderUseCaseTest {

    private MercadoPagoOrderPort orderPort;
    private PointOrderPersistencePort persistencePort;
    private ProcessPaymentOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderPort = mock(MercadoPagoOrderPort.class);
        persistencePort = mock(PointOrderPersistencePort.class);
        useCase = new ProcessPaymentOrderUseCase(orderPort, persistencePort);
    }

    @Test
    void shouldStartPaymentAndReturnResultadoPoint() {
        // Arrange
        String terminalId = "TERM_123";
        BigDecimal amount = new BigDecimal("45.50");
        String description = "Test Payment";
        String externalReference = "EXT_456";

        ResultadoPoint expectedResult = new ResultadoPoint("ORD_999", "created", null);
        when(orderPort.createOrderPoint(any(CobrancaPoint.class))).thenReturn(expectedResult);

        // Act
        ResultadoPoint result = useCase.startPayment(terminalId, amount, description, externalReference, "credit_card");

        // Assert
        assertNotNull(result);
        assertEquals("ORD_999", result.orderId());
        assertEquals("created", result.status());

        verify(orderPort, times(1)).createOrderPoint(argThat(cobranca ->
                cobranca.terminalId().equals(terminalId) &&
                cobranca.amount().equals(amount) &&
                cobranca.description().equals(description) &&
                cobranca.orderIdErp().equals(externalReference) &&
                cobranca.idempotencyKey() != null
        ));
    }
}
