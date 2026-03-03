package dev.kalles.sale.core.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.kalles.sale.core.enums.payment.PaymentMethod;

class PaymentFactoryTest {

    private PaymentFactory paymentFactory;

    @BeforeEach
    void setUp() {
        List<PaymentStrategy> strategies = List.of(
                new CashPaymentStrategy(),
                new PixPaymentStrategy(),
                new CreditCardPaymentStrategy(),
                new DebitCardPaymentStrategy()
        );
        paymentFactory = new PaymentFactory(strategies);
    }

    @Test
    @DisplayName("Should return CashPaymentStrategy for CASH method")
    void shouldReturnCashStrategy() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.CASH);
        assertInstanceOf(CashPaymentStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return PixPaymentStrategy for PIX method")
    void shouldReturnPixStrategy() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.PIX);
        assertInstanceOf(PixPaymentStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return CreditCardPaymentStrategy for CREDIT_CARD method")
    void shouldReturnCreditCardStrategy() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        assertInstanceOf(CreditCardPaymentStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return DebitCardPaymentStrategy for DEBIT_CARD method")
    void shouldReturnDebitCardStrategy() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.DEBIT_CARD);
        assertInstanceOf(DebitCardPaymentStrategy.class, strategy);
    }

    @Test
    @DisplayName("Cash strategy should return confirmed result")
    void cashStrategyShouldReturnConfirmed() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.CASH);
        PaymentResult result = strategy.process(new BigDecimal("100.00"));
        assertTrue(result.confirmed());
    }

    @Test
    @DisplayName("PIX strategy should return confirmed result with transaction ID")
    void pixStrategyShouldReturnConfirmedWithTransactionId() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.PIX);
        PaymentResult result = strategy.process(new BigDecimal("100.00"));
        assertTrue(result.confirmed());
        assertNotNull(result.transactionId());
    }

    @Test
    @DisplayName("Credit card strategy should return confirmed result with transaction ID")
    void creditCardStrategyShouldReturnConfirmedWithTransactionId() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        PaymentResult result = strategy.process(new BigDecimal("100.00"));
        assertTrue(result.confirmed());
        assertNotNull(result.transactionId());
    }

    @Test
    @DisplayName("Debit card strategy should return confirmed result with transaction ID")
    void debitCardStrategyShouldReturnConfirmedWithTransactionId() {
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.DEBIT_CARD);
        PaymentResult result = strategy.process(new BigDecimal("100.00"));
        assertTrue(result.confirmed());
        assertNotNull(result.transactionId());
    }
}
