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
import dev.kalles.sale.security.context.CompanyContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenSessionUseCase - Caso de Uso de Abertura de Sessão")
class OpenSessionUseCaseTest {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    @Mock
    private SessionValidator validatorChain;

    @Mock
    private PairedDeviceSessionGuard pairedDeviceSessionGuard;

    private OpenSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        useCase = new OpenSessionUseCase(
            cashRegisterRepository,
            operatorRepository,
            sessionRepository,
            validatorChain,
            pairedDeviceSessionGuard
        );
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve abrir sessão com sucesso")
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

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", operatorCode);
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, initialAmount);

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId(operatorCode, COMPANY_ID))
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
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verify(operatorRepository).findByCodeAndCompanyId(operatorCode, COMPANY_ID);

        ArgumentCaptor<CashRegisterSession> captor = ArgumentCaptor.forClass(CashRegisterSession.class);
        verify(sessionRepository).save(captor.capture());

        CashRegisterSession savedSession = captor.getValue();
        assertTrue(savedSession.isOpen());
        assertEquals(initialAmount, savedSession.getInitialAmountValue());
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

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.empty());

        // When & Then
        CashRegisterNotFoundException exception = assertThrows(
            CashRegisterNotFoundException.class,
            () -> useCase.execute(request)
        );

        assertEquals("Caixa não encontrado: PDV-99", exception.getMessage());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verifyNoInteractions(pairedDeviceSessionGuard);
        verifyNoInteractions(operatorRepository);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador não encontrado")
    void shouldThrowExceptionWhenOperatorNotFound() {
        // Given
        String cashRegisterCode = "PDV-01";
        String operatorCode = "OP999";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            operatorCode,
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", java.util.UUID.randomUUID());

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId(operatorCode, COMPANY_ID))
            .thenReturn(Optional.empty());

        // When & Then
        OperatorNotFoundException exception = assertThrows(
            OperatorNotFoundException.class,
            () -> useCase.execute(request)
        );

        assertEquals("Operador não encontrado: OP999", exception.getMessage());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verify(operatorRepository).findByCodeAndCompanyId(operatorCode, COMPANY_ID);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve chamar a cadeia de validação antes de processar")
    void shouldCallValidatorChainBeforeProcessing() {
        // Given
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));

        when(cashRegisterRepository.findByCodeAndCompanyId(anyString(), eq(COMPANY_ID)))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId(anyString(), eq(COMPANY_ID)))
            .thenReturn(Optional.of(operator));
        when(sessionRepository.save(any(CashRegisterSession.class)))
            .thenReturn(session);

        // When
        useCase.execute(request);

        // Then
        verify(validatorChain).validate(request);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
    }

    @Test
    @DisplayName("Deve bloquear abertura quando dispositivo nao esta pareado ao caixa")
    void shouldBlockOpenSessionWhenDeviceIsNotPaired() {
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00")
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        doThrow(new IllegalArgumentException("O dispositivo precisa estar pareado antes da operação."))
            .when(pairedDeviceSessionGuard)
            .ensureCanOperate(cashRegister);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(request)
        );

        assertEquals("O dispositivo precisa estar pareado antes da operação.", exception.getMessage());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId("PDV-01", COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verifyNoInteractions(operatorRepository);
        verifyNoInteractions(sessionRepository);
    }
}
