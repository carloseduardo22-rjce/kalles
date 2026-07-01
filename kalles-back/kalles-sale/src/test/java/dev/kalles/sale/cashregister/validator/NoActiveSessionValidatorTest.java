package dev.kalles.sale.cashregister.validator;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.exception.ActiveSessionAlreadyExistsException;
import dev.kalles.sale.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.specification.ActiveSessionSpecification;
import dev.kalles.sale.security.context.CompanyContextHolder;
import org.junit.jupiter.api.AfterEach;
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

    private static final UUID COMPANY_ID = UUID.fromString("ff6e3613-db5e-4aea-a333-f7a4e9f07804");

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private ActiveSessionSpecification activeSessionSpec;

    private NoActiveSessionValidator validator;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        validator = new NoActiveSessionValidator(cashRegisterRepository, activeSessionSpec);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
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

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(activeSessionSpec.isSatisfiedBy(cashRegister))
            .thenReturn(false);

        assertDoesNotThrow(() -> validator.validate(request));

        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
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

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(activeSessionSpec.isSatisfiedBy(cashRegister))
            .thenReturn(true);

        ActiveSessionAlreadyExistsException exception = assertThrows(
            ActiveSessionAlreadyExistsException.class,
            () -> validator.validate(request)
        );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("PDV-01"));

        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
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

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.empty());

        CashRegisterNotFoundException exception = assertThrows(
            CashRegisterNotFoundException.class,
            () -> validator.validate(request)
        );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("PDV-99"));

        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verifyNoInteractions(activeSessionSpec);
    }
}
