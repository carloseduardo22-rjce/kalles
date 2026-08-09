package dev.kalles.cashregister.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InitialAmount - Valor Inicial do Caixa")
class InitialAmountTest {

    @Test
    @DisplayName("Deve criar valor inicial válido")
    void shouldCreateValidInitialAmount() {
        // Given
        BigDecimal value = new BigDecimal("100.00");

        // When
        InitialAmount initialAmount = new InitialAmount(value);

        // Then
        assertNotNull(initialAmount);
        assertEquals(value, initialAmount.getValue());
    }

    @Test
    @DisplayName("Deve criar valor inicial com zero")
    void shouldCreateInitialAmountWithZero() {
        // Given
        BigDecimal value = BigDecimal.ZERO;

        // When
        InitialAmount initialAmount = new InitialAmount(value);

        // Then
        assertNotNull(initialAmount);
        assertEquals(BigDecimal.ZERO, initialAmount.getValue());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é nulo")
    void shouldThrowExceptionWhenValueIsNull() {
        // Given
        BigDecimal value = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new InitialAmount(value)
        );

        assertEquals("Valor inicial não pode ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é negativo")
    void shouldThrowExceptionWhenValueIsNegative() {
        // Given
        BigDecimal value = new BigDecimal("-10.00");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new InitialAmount(value)
        );

        assertEquals("Valor inicial não pode ser negativo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve ser igual quando os valores são os mesmos")
    void shouldBeEqualWhenValuesAreTheSame() {
        // Given
        BigDecimal value = new BigDecimal("100.00");
        InitialAmount amount1 = new InitialAmount(value);
        InitialAmount amount2 = new InitialAmount(value);

        // When & Then
        assertEquals(amount1, amount2);
        assertEquals(amount1.hashCode(), amount2.hashCode());
    }

    @Test
    @DisplayName("Não deve ser igual quando os valores são diferentes")
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Given
        InitialAmount amount1 = new InitialAmount(new BigDecimal("100.00"));
        InitialAmount amount2 = new InitialAmount(new BigDecimal("200.00"));

        // When & Then
        assertNotEquals(amount1, amount2);
    }
}
