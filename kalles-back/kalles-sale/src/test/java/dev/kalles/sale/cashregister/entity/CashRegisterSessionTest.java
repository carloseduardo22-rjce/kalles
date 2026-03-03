package dev.kalles.sale.cashregister.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CashRegisterSessionTest {

    @Test
    void shouldCreateSessionUsingFactoryMethod() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");
        BigDecimal initialAmount = new BigDecimal("100.00");

        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, initialAmount);

        assertNotNull(session);
        assertTrue(session.isOpen());
        assertEquals(cashRegister, session.getCashRegister());
        assertEquals(operator, session.getOperator());
        assertEquals(initialAmount, session.getInitialAmountValue());
        assertNotNull(session.getOpenedAt());
        assertNull(session.getClosedAt());
    }

    @Test
    void shouldThrowExceptionWhenCashRegisterIsNull() {
        Operator operator = new Operator("João Silva", "OP001");
        BigDecimal initialAmount = new BigDecimal("100.00");

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> CashRegisterSession.open(null, operator, initialAmount)
        );

        assertEquals("Caixa obrigatório", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorIsNull() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        BigDecimal initialAmount = new BigDecimal("100.00");

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> CashRegisterSession.open(cashRegister, null, initialAmount)
        );

        assertEquals("Operador obrigatório", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInitialAmountIsNull() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");

        assertThrows(
            IllegalArgumentException.class,
            () -> CashRegisterSession.open(cashRegister, operator, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenInitialAmountIsNegative() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");
        BigDecimal initialAmount = new BigDecimal("-50.00");

        assertThrows(
            IllegalArgumentException.class,
            () -> CashRegisterSession.open(cashRegister, operator, initialAmount)
        );
    }

    @Test
    void shouldCloseSessionSuccessfully() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));

        session.close();

        assertFalse(session.isOpen());
        assertNotNull(session.getClosedAt());
    }

    @Test
    void shouldThrowExceptionWhenClosingAlreadyClosedSession() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
        session.close();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> session.close()
        );

        assertEquals("Sessão já está fechada", exception.getMessage());
    }
}
