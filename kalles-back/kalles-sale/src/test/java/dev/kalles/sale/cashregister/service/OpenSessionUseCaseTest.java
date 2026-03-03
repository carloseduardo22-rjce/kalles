package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.dto.SessionResponse;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.sale.cashregister.exception.OperatorNotFoundException;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.cashregister.validator.SessionValidator;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenSessionUseCaseTest {

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    @Mock
    private SessionValidator validatorChain;

    private OpenSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OpenSessionUseCase(
            cashRegisterRepository,
            operatorRepository,
            sessionRepository,
            validatorChain
        );
    }

    @Test
    void shouldOpenSessionSuccessfully() {
        // Given
        String cashRegisterCode = "PDV-01";
        String operatorCode = "OP001";
        BigDecimal initialAmount = new BigDecimal("100.00");

        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            operatorCode,
            initialAmount
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal");
        Operator operator = new Operator("João Silva", operatorCode);
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, initialAmount);

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCode(operatorCode))
            .thenReturn(Optional.of(operator));
        when(sessionRepository.save(any(CashRegisterSession.class)))
            .thenReturn(session);

        // When
        SessionResponse response = useCase.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(cashRegisterCode, response.cashRegisterCode());
        assertEquals("João Silva", response.operatorName());
        assertEquals(initialAmount, response.initialAmount());
        assertEquals(SessionStatus.OPEN.name(), response.status());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verify(operatorRepository).findByCode(operatorCode);

        ArgumentCaptor<CashRegisterSession> captor = ArgumentCaptor.forClass(CashRegisterSession.class);
        verify(sessionRepository).save(captor.capture());

        CashRegisterSession savedSession = captor.getValue();
        assertTrue(savedSession.isOpen());
        assertEquals(initialAmount, savedSession.getInitialAmountValue());
    }

    @Test
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
            () -> useCase.execute(request)
        );

        assertEquals("Caixa não encontrado: PDV-99", exception.getMessage());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verifyNoInteractions(operatorRepository);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    void shouldThrowExceptionWhenOperatorNotFound() {
        // Given
        String cashRegisterCode = "PDV-01";
        String operatorCode = "OP999";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            operatorCode,
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal");

        when(cashRegisterRepository.findByCode(cashRegisterCode))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCode(operatorCode))
            .thenReturn(Optional.empty());

        // When & Then
        OperatorNotFoundException exception = assertThrows(
            OperatorNotFoundException.class,
            () -> useCase.execute(request)
        );

        assertEquals("Operador não encontrado: OP999", exception.getMessage());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCode(cashRegisterCode);
        verify(operatorRepository).findByCode(operatorCode);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    void shouldCallValidatorChainBeforeProcessing() {
        // Given
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));

        when(cashRegisterRepository.findByCode(anyString()))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCode(anyString()))
            .thenReturn(Optional.of(operator));
        when(sessionRepository.save(any(CashRegisterSession.class)))
            .thenReturn(session);

        // When
        useCase.execute(request);

        // Then
        verify(validatorChain).validate(request);
    }
}
