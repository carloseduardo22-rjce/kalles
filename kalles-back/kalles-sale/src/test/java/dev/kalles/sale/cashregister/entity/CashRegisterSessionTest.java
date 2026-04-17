package dev.kalles.sale.cashregister.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashRegisterSession - Sessão de Caixa")
class CashRegisterSessionTest {

    @Test
    @DisplayName("Deve criar sessão usando o método de fábrica")
    void shouldCreateSessionUsingFactoryMethod() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
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
    @DisplayName("Deve abrir sessao em modo somente dinheiro e bloquear pagamentos eletronicos")
    void shouldOpenCashOnlySessionAndDisableElectronicPayments() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");

        CashRegisterSession session = CashRegisterSession.open(
            cashRegister,
            operator,
            new BigDecimal("100.00"),
            true
        );

        assertTrue(session.isCashOnlyOperation());
        assertFalse(session.allowsElectronicPayments());
    }

    @Test
    @DisplayName("Deve permitir pagamentos eletronicos quando a sessao nao e somente dinheiro")
    void shouldAllowElectronicPaymentsWhenSessionIsNotCashOnly() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");

        CashRegisterSession session = CashRegisterSession.open(
            cashRegister,
            operator,
            new BigDecimal("100.00"),
            false
        );

        assertFalse(session.isCashOnlyOperation());
        assertTrue(session.allowsElectronicPayments());
    }

    @Test
    @DisplayName("Deve lançar exceção quando caixa é nulo")
    void shouldThrowExceptionWhenCashRegisterIsNull() {
        Operator operator = new Operator("João Silva", "OP001");
        BigDecimal initialAmount = new BigDecimal("100.00");

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> CashRegisterSession.open(null, operator, initialAmount)
        );

        assertEquals("Caixa obrigatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador é nulo")
    void shouldThrowExceptionWhenOperatorIsNull() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        BigDecimal initialAmount = new BigDecimal("100.00");

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> CashRegisterSession.open(cashRegister, null, initialAmount)
        );

        assertEquals("Operador obrigatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor inicial é nulo")
    void shouldThrowExceptionWhenInitialAmountIsNull() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");

        assertThrows(
            IllegalArgumentException.class,
            () -> CashRegisterSession.open(cashRegister, operator, null)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor inicial é negativo")
    void shouldThrowExceptionWhenInitialAmountIsNegative() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");
        BigDecimal initialAmount = new BigDecimal("-50.00");

        assertThrows(
            IllegalArgumentException.class,
            () -> CashRegisterSession.open(cashRegister, operator, initialAmount)
        );
    }

    @Test
    @DisplayName("Deve fechar sessão com sucesso")
    void shouldCloseSessionSuccessfully() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));

        session.close();

        assertFalse(session.isOpen());
        assertNotNull(session.getClosedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção ao fechar sessão já fechada")
    void shouldThrowExceptionWhenClosingAlreadyClosedSession() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
        session.close();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> session.close()
        );

        assertEquals("Sessao ja esta fechada", exception.getMessage());
    }
}
