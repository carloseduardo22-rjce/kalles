package dev.kalles.sale.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Strategies simuladas - gate por configuração")
class SimulatedPaymentStrategiesTest {

    private static final BigDecimal AMOUNT = new BigDecimal("50.00");

    @Test
    @DisplayName("Com simulação desabilitada (default de produção), cartão e PIX devem ser rejeitados")
    void shouldRejectSimulatedPaymentsWhenDisabled() {
        assertThrows(IllegalStateException.class, () -> new CreditCardPaymentStrategy(false).process(AMOUNT));
        assertThrows(IllegalStateException.class, () -> new DebitCardPaymentStrategy(false).process(AMOUNT));
        assertThrows(IllegalStateException.class, () -> new PixPaymentStrategy(false).process(AMOUNT));
    }

    @Test
    @DisplayName("Com simulação habilitada (dev), pagamentos são confirmados com transactionId simulado")
    void shouldConfirmSimulatedPaymentsWhenEnabled() {
        PaymentResult credit = new CreditCardPaymentStrategy(true).process(AMOUNT);
        PaymentResult debit = new DebitCardPaymentStrategy(true).process(AMOUNT);
        PaymentResult pix = new PixPaymentStrategy(true).process(AMOUNT);

        assertTrue(credit.confirmed());
        assertTrue(debit.confirmed());
        assertTrue(pix.confirmed());
        assertNotNull(credit.transactionId());
        assertNotNull(debit.transactionId());
        assertNotNull(pix.transactionId());
    }

    @Test
    @DisplayName("Dinheiro nunca é bloqueado: não depende de integração externa")
    void cashShouldAlwaysBeAccepted() {
        PaymentResult cash = new CashPaymentStrategy().process(AMOUNT);
        assertEquals(true, cash.confirmed());
    }
}
