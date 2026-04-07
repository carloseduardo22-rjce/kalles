package dev.kalles.sale.cashregister.validator;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.exception.ActiveSessionAlreadyExistsException;
import dev.kalles.sale.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.specification.ActiveSessionSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoActiveSessionValidator - Validador de Sessão Ativa")
class NoActiveSessionValidatorTest {

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private ActiveSessionSpecification activeSessionSpec;

    private NoActiveSessionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NoActiveSessionValidator(cashRegisterRepository, activeSessionSpec);
    }

    @Test
    @DisplayName("Deve passar a validação quando não existe sessão ativa")
    void shouldPassWhenNoActiveSessionExists() {
        // Given
        String cashRegisterCode = "PDV-01";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            "OP001",
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", java.util.UUID.randomUUID());

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.of(cashRegister));
        when(activeSessionSpec.isSatisfiedBy(cashRegister))
            .thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));

        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verify(activeSessionSpec).isSatisfiedBy(cashRegister);
    }

    @Test
    @DisplayName("Deve lançar exceção quando existe sessão ativa")
    void shouldThrowExceptionWhenActiveSessionExists() {
        // Given
        String cashRegisterCode = "PDV-01";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            "OP001",
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", java.util.UUID.randomUUID());

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.of(cashRegister));
        when(activeSessionSpec.isSatisfiedBy(cashRegister))
            .thenReturn(true);

        // When & Then
        ActiveSessionAlreadyExistsException exception = assertThrows(
            ActiveSessionAlreadyExistsException.class,
            () -> validator.validate(request)
        );

        assertEquals("O caixa PDV-01 já possui uma sessão ativa", exception.getMessage());

        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verify(activeSessionSpec).isSatisfiedBy(cashRegister);
    }

    @Test
    @DisplayName("Deve lançar exceção quando caixa não encontrado")
    void shouldThrowExceptionWhenCashRegisterNotFound() {
        // Given
        String cashRegisterCode = "PDV-99";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            "OP001",
            new BigDecimal("100.00")
        );

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.empty());

        // When & Then
        CashRegisterNotFoundException exception = assertThrows(
            CashRegisterNotFoundException.class,
            () -> validator.validate(request)
        );

        assertEquals("Caixa não encontrado: PDV-99", exception.getMessage());

        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verifyNoInteractions(activeSessionSpec);
    }
}
