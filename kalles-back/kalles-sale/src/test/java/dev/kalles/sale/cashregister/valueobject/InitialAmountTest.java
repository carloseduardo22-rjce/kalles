package dev.kalles.sale.cashregister.valueobject;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InitialAmountTest {

    @Test
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
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Given
        InitialAmount amount1 = new InitialAmount(new BigDecimal("100.00"));
        InitialAmount amount2 = new InitialAmount(new BigDecimal("200.00"));

        // When & Then
        assertNotEquals(amount1, amount2);
    }
}
