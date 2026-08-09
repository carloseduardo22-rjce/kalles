package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.dto.SessionResponse;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.exception.OperatorNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.validator.SessionValidator;
import dev.kalles.cashregister.valueobject.SessionStatus;
import dev.kalles.security.context.CompanyContextHolder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenSessionUseCase - Caso de Uso de Abertura de Sessao")
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

    @Mock
    private CashRegisterPaymentIntegrationService paymentIntegrationService;

    private OpenSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        useCase = new OpenSessionUseCase(
            cashRegisterRepository,
            operatorRepository,
            sessionRepository,
            validatorChain,
            pairedDeviceSessionGuard,
            paymentIntegrationService
        );
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve abrir sessao com sucesso quando pagamento esta configurado")
    void shouldOpenSessionSuccessfully() {
        String cashRegisterCode = "PDV-01";
        String operatorCode = "OP001";
        BigDecimal initialAmount = new BigDecimal("100.00");

        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            operatorCode,
            initialAmount,
            false
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", UUID.randomUUID());
        Operator operator = new Operator("Joao Silva", operatorCode);
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, initialAmount);

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId(operatorCode, COMPANY_ID))
            .thenReturn(Optional.of(operator));
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(true);
        when(sessionRepository.save(any(CashRegisterSession.class))).thenReturn(session);

        SessionResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(cashRegisterCode, response.cashRegisterCode());
        assertEquals("Joao Silva", response.operatorName());
        assertEquals(initialAmount, response.initialAmount());
        assertEquals(SessionStatus.OPEN.name(), response.status());
        assertFalse(response.cashOnlyOperation());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verify(operatorRepository).findByCodeAndCompanyId(operatorCode, COMPANY_ID);
        verify(paymentIntegrationService).isPaymentIntegrationConfigured(cashRegister);

        ArgumentCaptor<CashRegisterSession> captor = ArgumentCaptor.forClass(CashRegisterSession.class);
        verify(sessionRepository).save(captor.capture());

        CashRegisterSession savedSession = captor.getValue();
        assertTrue(savedSession.isOpen());
        assertEquals(initialAmount, savedSession.getInitialAmountValue());
        assertFalse(savedSession.isCashOnlyOperation());
    }

    @Test
    @DisplayName("Deve permitir abertura em modo somente dinheiro quando confirmado")
    void shouldOpenCashOnlySessionWhenExplicitlyAllowed() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());
        Operator operator = new Operator("Joao Silva", "OP001");
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00"),
            true
        );

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId("OP001", COMPANY_ID))
            .thenReturn(Optional.of(operator));
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(false);
        when(sessionRepository.save(any(CashRegisterSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = useCase.execute(request);

        assertTrue(response.cashOnlyOperation());
    }

    @Test
    @DisplayName("Deve bloquear abertura sem configuracao de pagamento e sem confirmacao")
    void shouldBlockOpenSessionWhenPaymentIsNotConfiguredAndCashOnlyNotAllowed() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());
        Operator operator = new Operator("Joao Silva", "OP001");
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00"),
            false
        );

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId("OP001", COMPANY_ID))
            .thenReturn(Optional.of(operator));
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(false);
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.execute(request)
        );

        assertTrue(exception.getMessage().contains("Pagamento"));
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
            () -> useCase.execute(request)
        );

        assertTrue(exception.getMessage().contains("PDV-99"));

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verifyNoInteractions(pairedDeviceSessionGuard);
        verifyNoInteractions(operatorRepository);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve lancar excecao quando operador nao encontrado")
    void shouldThrowExceptionWhenOperatorNotFound() {
        String cashRegisterCode = "PDV-01";
        String operatorCode = "OP999";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            operatorCode,
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", UUID.randomUUID());

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId(operatorCode, COMPANY_ID))
            .thenReturn(Optional.empty());

        OperatorNotFoundException exception = assertThrows(
            OperatorNotFoundException.class,
            () -> useCase.execute(request)
        );

        assertTrue(exception.getMessage().contains("OP999"));

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verify(operatorRepository).findByCodeAndCompanyId(operatorCode, COMPANY_ID);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve chamar a cadeia de validacao antes de processar")
    void shouldCallValidatorChainBeforeProcessing() {
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());
        Operator operator = new Operator("Joao Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));

        when(cashRegisterRepository.findByCodeAndCompanyId(anyString(), eq(COMPANY_ID)))
            .thenReturn(Optional.of(cashRegister));
        when(operatorRepository.findByCodeAndCompanyId(anyString(), eq(COMPANY_ID)))
            .thenReturn(Optional.of(operator));
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(true);
        when(sessionRepository.save(any(CashRegisterSession.class))).thenReturn(session);

        useCase.execute(request);

        verify(validatorChain).validate(request);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
    }

    @Test
    @DisplayName("Deve bloquear abertura quando dispositivo nao esta pareado ao caixa")
    void shouldBlockOpenSessionWhenDeviceIsNotPaired() {
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            "OP001",
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        doThrow(new IllegalArgumentException("O dispositivo precisa estar pareado antes da operaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o."))
            .when(pairedDeviceSessionGuard)
            .ensureCanOperate(cashRegister);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(request)
        );

        assertEquals("O dispositivo precisa estar pareado antes da operaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.", exception.getMessage());

        verify(validatorChain).validate(request);
        verify(cashRegisterRepository).findByCodeAndCompanyId("PDV-01", COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verifyNoInteractions(operatorRepository);
        verifyNoInteractions(sessionRepository);
    }
}


