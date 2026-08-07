package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.OperatorAlreadyInSessionException;
import dev.kalles.cashregister.exception.OperatorNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.valueobject.SessionStatus;
import dev.kalles.security.context.CompanyContextHolder;
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
@DisplayName("NoActiveOperatorSessionValidator - Validador de operador com sessao ativa")
class NoActiveOperatorSessionValidatorTest {

    private static final UUID COMPANY_ID = UUID.fromString("bd60fd49-34d8-4d9c-ae58-62d290e8683c");

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    private NoActiveOperatorSessionValidator validator;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        validator = new NoActiveOperatorSessionValidator(operatorRepository, sessionRepository);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve passar quando operador da filial nao possui sessao ativa")
    void shouldPassWhenOperatorHasNoActiveSession() {
        OpenSessionRequest request = new OpenSessionRequest("PDV-01", "OP001", new BigDecimal("100.00"), false);
        Operator operator = new Operator("Operador", "OP001");

        when(operatorRepository.findByCodeAndCompanyId("OP001", COMPANY_ID)).thenReturn(Optional.of(operator));
        when(sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validate(request));

        verify(operatorRepository).findByCodeAndCompanyId("OP001", COMPANY_ID);
        verify(sessionRepository).existsByOperatorAndStatus(operator, SessionStatus.OPEN);
    }

    @Test
    @DisplayName("Deve bloquear quando operador da filial ja possui sessao ativa")
    void shouldThrowWhenOperatorAlreadyHasActiveSession() {
        OpenSessionRequest request = new OpenSessionRequest("PDV-01", "OP001", new BigDecimal("100.00"), false);
        Operator operator = new Operator("Operador", "OP001");

        when(operatorRepository.findByCodeAndCompanyId("OP001", COMPANY_ID)).thenReturn(Optional.of(operator));
        when(sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)).thenReturn(true);

        OperatorAlreadyInSessionException exception = assertThrows(
                OperatorAlreadyInSessionException.class,
                () -> validator.validate(request)
        );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("OP001"));
        verify(operatorRepository).findByCodeAndCompanyId("OP001", COMPANY_ID);
        verify(sessionRepository).existsByOperatorAndStatus(operator, SessionStatus.OPEN);
    }

    @Test
    @DisplayName("Deve rejeitar operador inexistente na filial ativa")
    void shouldThrowWhenOperatorIsNotFoundInCurrentCompany() {
        OpenSessionRequest request = new OpenSessionRequest("PDV-01", "OP404", new BigDecimal("100.00"), false);

        when(operatorRepository.findByCodeAndCompanyId("OP404", COMPANY_ID)).thenReturn(Optional.empty());

        OperatorNotFoundException exception = assertThrows(
                OperatorNotFoundException.class,
                () -> validator.validate(request)
        );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("OP404"));
        verify(operatorRepository).findByCodeAndCompanyId("OP404", COMPANY_ID);
        verifyNoInteractions(sessionRepository);
    }
}
