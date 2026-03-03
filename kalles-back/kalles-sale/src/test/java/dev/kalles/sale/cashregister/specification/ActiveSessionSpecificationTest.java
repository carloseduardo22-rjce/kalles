package dev.kalles.sale.cashregister.specification;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveSessionSpecificationTest {

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    private ActiveSessionSpecification specification;

    @BeforeEach
    void setUp() {
        specification = new ActiveSessionSpecification(sessionRepository);
    }

    @Test
    void shouldReturnTrueWhenActiveSessionExists() {
        // Given
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");

        when(sessionRepository.existsByCashRegisterAndStatus(cashRegister, SessionStatus.OPEN))
            .thenReturn(true);

        // When
        boolean result = specification.isSatisfiedBy(cashRegister);

        // Then
        assertTrue(result);
        verify(sessionRepository).existsByCashRegisterAndStatus(cashRegister, SessionStatus.OPEN);
    }

    @Test
    void shouldReturnFalseWhenNoActiveSessionExists() {
        // Given
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");

        when(sessionRepository.existsByCashRegisterAndStatus(cashRegister, SessionStatus.OPEN))
            .thenReturn(false);

        // When
        boolean result = specification.isSatisfiedBy(cashRegister);

        // Then
        assertFalse(result);
        verify(sessionRepository).existsByCashRegisterAndStatus(cashRegister, SessionStatus.OPEN);
    }
}
