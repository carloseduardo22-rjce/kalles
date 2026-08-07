package dev.kalles.cashregister.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SessionPeriod - Período de Sessão do Caixa")
class SessionPeriodTest {

    @Test
    @DisplayName("Deve criar período de sessão com data de abertura")
    void shouldCreateSessionPeriodWithOpenedAt() {
        // Given
        LocalDateTime openedAt = LocalDateTime.of(2026, 1, 28, 10, 0);

        // When
        SessionPeriod period = new SessionPeriod(openedAt);

        // Then
        assertNotNull(period);
        assertEquals(openedAt, period.getOpenedAt());
        assertNull(period.getClosedAt());
        assertTrue(period.isOpen());
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de abertura é nula")
    void shouldThrowExceptionWhenOpenedAtIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new SessionPeriod(null)
        );

        assertEquals("Data de abertura obrigatória", exception.getMessage());
    }

    @Test
    @DisplayName("Deve fechar sessão com sucesso")
    void shouldCloseSessionSuccessfully() {
        // Given
        LocalDateTime openedAt = LocalDateTime.of(2026, 1, 28, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2026, 1, 28, 18, 0);
        SessionPeriod period = new SessionPeriod(openedAt);

        // When
        period.close(closedAt);

        // Then
        assertFalse(period.isOpen());
        assertEquals(closedAt, period.getClosedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção ao fechar sessão já fechada")
    void shouldThrowExceptionWhenClosingAlreadyClosedSession() {
        // Given
        LocalDateTime openedAt = LocalDateTime.of(2026, 1, 28, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2026, 1, 28, 18, 0);
        SessionPeriod period = new SessionPeriod(openedAt);
        period.close(closedAt);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> period.close(LocalDateTime.of(2026, 1, 28, 19, 0))
        );

        assertEquals("Sessão já está fechada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de fechamento é anterior à abertura")
    void shouldThrowExceptionWhenClosedAtIsBeforeOpenedAt() {
        // Given
        LocalDateTime openedAt = LocalDateTime.of(2026, 1, 28, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2026, 1, 28, 9, 0); // antes da abertura
        SessionPeriod period = new SessionPeriod(openedAt);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> period.close(closedAt)
        );

        assertEquals("Data de fechamento anterior à abertura", exception.getMessage());
    }
}
