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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoActiveSessionValidator - Validador de Sessao Ativa")
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
    @DisplayName("Deve passar a validacao quando nao existe sessao ativa")
    void shouldPassWhenNoActiveSessionExists() {
        String cashRegisterCode = "PDV-01";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            "OP001",
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", UUID.randomUUID());

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.of(cashRegister));
        when(activeSessionSpec.isSatisfiedBy(cashRegister))
            .thenReturn(false);

        assertDoesNotThrow(() -> validator.validate(request));

        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verify(activeSessionSpec).isSatisfiedBy(cashRegister);
    }

    @Test
    @DisplayName("Deve lancar excecao quando existe sessao ativa")
    void shouldThrowExceptionWhenActiveSessionExists() {
        String cashRegisterCode = "PDV-01";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            "OP001",
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", UUID.randomUUID());

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.of(cashRegister));
        when(activeSessionSpec.isSatisfiedBy(cashRegister))
            .thenReturn(true);

        ActiveSessionAlreadyExistsException exception = assertThrows(
            ActiveSessionAlreadyExistsException.class,
            () -> validator.validate(request)
        );

        assertEquals("O caixa PDV-01 já possui uma sessão ativa", exception.getMessage());

        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verify(activeSessionSpec).isSatisfiedBy(cashRegister);
    }

    @Test
    @DisplayName("Deve lancar excecao quando caixa nao encontrado")
    void shouldThrowExceptionWhenCashRegisterNotFound() {
        String cashRegisterCode = "PDV-99";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            "OP001",
            new BigDecimal("100.00"),
            false
        );

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.empty());

        CashRegisterNotFoundException exception = assertThrows(
            CashRegisterNotFoundException.class,
            () -> validator.validate(request)
        );

        assertEquals("Caixa não encontrado: PDV-99", exception.getMessage());

        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verifyNoInteractions(activeSessionSpec);
    }
}


