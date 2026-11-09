package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.dto.SessionResponse;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.exception.OperatorAccountNotLinkedException;
import dev.kalles.cashregister.exception.PaymentIntegrationNotConfiguredException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.validator.SessionValidator;
import dev.kalles.cashregister.valueobject.SessionStatus;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
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

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.company(COMPANY_ID);

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private SessionOperatorResolver sessionOperatorResolver;

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
        useCase = new OpenSessionUseCase(
            cashRegisterRepository,
            sessionOperatorResolver,
            sessionRepository,
            List.of(validatorChain),
            pairedDeviceSessionGuard,
            paymentIntegrationService
        );
    }

    @Test
    @DisplayName("Deve abrir sessao com sucesso quando pagamento esta configurado")
    void shouldOpenSessionSuccessfully() {
        String cashRegisterCode = "PDV-01";
        String operatorCode = "OP001";
        BigDecimal initialAmount = new BigDecimal("100.00");

        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            initialAmount,
            false
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", UUID.randomUUID());
        Operator operator = new Operator("Joao Silva", operatorCode);
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, initialAmount);

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(sessionOperatorResolver.resolve(COMPANY_ID)).thenReturn(operator);
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(true);
        when(sessionRepository.saveAndFlush(any(CashRegisterSession.class))).thenReturn(session);

        SessionResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(cashRegisterCode, response.cashRegisterCode());
        assertEquals("Joao Silva", response.operatorName());
        assertEquals(initialAmount, response.initialAmount());
        assertEquals(SessionStatus.OPEN.name(), response.status());
        assertFalse(response.cashOnlyOperation());

        verify(validatorChain).validate(request, operator);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verify(sessionOperatorResolver).resolve(COMPANY_ID);
        verify(paymentIntegrationService).isPaymentIntegrationConfigured(cashRegister);

        ArgumentCaptor<CashRegisterSession> captor = ArgumentCaptor.forClass(CashRegisterSession.class);
        verify(sessionRepository).saveAndFlush(captor.capture());

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
            new BigDecimal("100.00"),
            true
        );

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(sessionOperatorResolver.resolve(COMPANY_ID)).thenReturn(operator);
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(false);
        when(sessionRepository.saveAndFlush(any(CashRegisterSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
            new BigDecimal("100.00"),
            false
        );

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(sessionOperatorResolver.resolve(COMPANY_ID)).thenReturn(operator);
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(false);
        PaymentIntegrationNotConfiguredException exception = assertThrows(
            PaymentIntegrationNotConfiguredException.class,
            () -> useCase.execute(request)
        );

        assertEquals("PAYMENT_INTEGRATION_NOT_CONFIGURED", exception.getCode());
    }

    @Test
    @DisplayName("Deve lancar excecao quando caixa nao encontrado")
    void shouldThrowExceptionWhenCashRegisterNotFound() {
        String cashRegisterCode = "PDV-99";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
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

        verifyNoInteractions(validatorChain);
        verify(cashRegisterRepository).findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID);
        verifyNoInteractions(pairedDeviceSessionGuard);
        verifyNoInteractions(sessionOperatorResolver);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve recusar abertura quando a conta nao tem cadastro de operador")
    void shouldRejectOpeningWhenAccountHasNoOperator() {
        String cashRegisterCode = "PDV-01";
        OpenSessionRequest request = new OpenSessionRequest(
            cashRegisterCode,
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister(cashRegisterCode, "Caixa Principal", UUID.randomUUID());

        when(cashRegisterRepository.findByCodeAndCompanyId(cashRegisterCode, COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        when(sessionOperatorResolver.resolve(COMPANY_ID)).thenThrow(new OperatorAccountNotLinkedException());

        assertThrows(OperatorAccountNotLinkedException.class, () -> useCase.execute(request));

        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verifyNoInteractions(validatorChain);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve chamar a cadeia de validacao antes de processar")
    void shouldCallValidatorChainBeforeProcessing() {
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());
        Operator operator = new Operator("Joao Silva", "OP001");
        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));

        when(cashRegisterRepository.findByCodeAndCompanyId(anyString(), eq(COMPANY_ID)))
            .thenReturn(Optional.of(cashRegister));
        when(sessionOperatorResolver.resolve(COMPANY_ID)).thenReturn(operator);
        when(paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister)).thenReturn(true);
        when(sessionRepository.saveAndFlush(any(CashRegisterSession.class))).thenReturn(session);

        useCase.execute(request);

        verify(validatorChain).validate(request, operator);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
    }

    @Test
    @DisplayName("Deve bloquear abertura quando dispositivo nao esta pareado ao caixa")
    void shouldBlockOpenSessionWhenDeviceIsNotPaired() {
        OpenSessionRequest request = new OpenSessionRequest(
            "PDV-01",
            new BigDecimal("100.00"),
            false
        );

        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());

        when(cashRegisterRepository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
            .thenReturn(Optional.of(cashRegister));
        doThrow(new ForbiddenOperationException("O dispositivo precisa estar pareado antes da operação."))
            .when(pairedDeviceSessionGuard)
            .ensureCanOperate(cashRegister);

        ForbiddenOperationException exception = assertThrows(
            ForbiddenOperationException.class,
            () -> useCase.execute(request)
        );

        assertEquals("O dispositivo precisa estar pareado antes da operação.", exception.getMessage());

        verifyNoInteractions(validatorChain);
        verify(cashRegisterRepository).findByCodeAndCompanyId("PDV-01", COMPANY_ID);
        verify(pairedDeviceSessionGuard).ensureCanOperate(cashRegister);
        verifyNoInteractions(sessionOperatorResolver);
        verifyNoInteractions(sessionRepository);
    }
}


