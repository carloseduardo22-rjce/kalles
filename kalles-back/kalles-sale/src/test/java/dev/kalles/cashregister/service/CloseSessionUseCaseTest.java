package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CloseSessionRequest;
import dev.kalles.cashregister.dto.CloseSessionResponse;
import dev.kalles.cashregister.dto.SessionSummaryResponse;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterClosing;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.exception.ClosingAuthorizationRequiredException;
import dev.kalles.cashregister.exception.PendingSalesBlockClosingException;
import dev.kalles.cashregister.exception.SessionAlreadyClosedException;
import dev.kalles.cashregister.exception.SessionOwnedByAnotherAccountException;
import dev.kalles.cashregister.repository.CashRegisterClosingRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.company.service.CompanyService;
import dev.kalles.product.entity.Product;
import dev.kalles.sale.dto.SessionPaymentMethodTotal;
import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.service.AuthenticatedAccountProvider;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.exception.NotFoundException;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloseSessionUseCase - Caso de uso de fechamento")
class CloseSessionUseCaseTest {

    private static final UUID COMPANY_ID = UUID.fromString("99f449b5-3f12-48f6-b4a7-dfa165ed39d7");
    private static final UUID OWNER_ACCOUNT_ID = UUID.fromString("5b1d8c3e-7a2f-4e61-9c0d-2f4a6b8e1d37");

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.company(COMPANY_ID);

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    @Mock
    private CashRegisterClosingRepository closingRepository;

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PairedDeviceSessionGuard pairedDeviceSessionGuard;

    @Mock
    private CompanyService companyService;

    @Mock
    private AuthenticatedAccountProvider authenticatedAccountProvider;

    private CloseSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CloseSessionUseCase(
                sessionRepository,
                closingRepository,
                operatorRepository,
                saleRepository,
                pairedDeviceSessionGuard,
                companyService,
                new PermissionService(),
                authenticatedAccountProvider
        );
    }

    @Test
    @DisplayName("Deve fechar sessao persistindo o snapshot do fechamento")
    void shouldCloseSessionPersistingClosingSnapshot() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        stubSummaryOf(sessionId, 1L, 2L, new BigDecimal("80.00"));
        stubTolerance("0.00");

        CloseSessionResponse response = useCase.execute(
                sessionId,
                new CloseSessionRequest(null, new BigDecimal("180.00"))
        );

        ArgumentCaptor<CashRegisterClosing> closingCaptor = ArgumentCaptor.forClass(CashRegisterClosing.class);
        verify(closingRepository).save(closingCaptor.capture());

        CashRegisterClosing savedClosing = closingCaptor.getValue();
        assertEquals(new BigDecimal("80.00"), savedClosing.getCashSalesAmount());
        assertEquals(new BigDecimal("180.00"), savedClosing.getExpectedCashAmount());
        assertEquals(new BigDecimal("180.00"), savedClosing.getCountedCashAmount());
        assertEquals(1, savedClosing.getCompletedSalesCount());
        assertEquals(2, savedClosing.getCanceledSalesCount());
        assertEquals(new BigDecimal("80.00"), savedClosing.getTotalSoldAmount());
        assertEquals(0, savedClosing.getCashDifferenceAmount().compareTo(BigDecimal.ZERO));
        assertNull(savedClosing.getAuthorizedByOperator());
        assertNull(response.nomeOperadorAutorizador());
        assertEquals(1, response.resumo().vendasConcluidas());
        assertEquals(2, response.resumo().vendasCanceladas());
        assertEquals(new BigDecimal("180.00"), response.resumo().saldoEsperadoEmCaixa());
        assertEquals(0, response.resumo().diferencaEmCaixa().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve retornar fechamento persistido ao consultar relatorio")
    void shouldReturnPersistedClosingOnReport() {
        CashRegisterSession session = buildOpenSession();
        session.close();
        UUID sessionId = UUID.randomUUID();
        Operator authorizer = buildAuthorizer();
        SessionSummaryResponse summary = new SessionSummaryResponse(
                2,
                1,
                new BigDecimal("250.00"),
                java.util.Map.of("CASH", new BigDecimal("120.00")),
                new BigDecimal("120.00"),
                new BigDecimal("220.00"),
                new BigDecimal("210.00"),
                new BigDecimal("-10.00")
        );
        CashRegisterClosing closing = CashRegisterClosing.create(
                session,
                authorizer,
                summary,
                new BigDecimal("210.00")
        );

        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(closingRepository.findBySession(session)).thenReturn(Optional.of(closing));

        SessionSummaryResponse report = useCase.getReport(sessionId);

        assertEquals(new BigDecimal("210.00"), report.valorInformadoEmCaixa());
        assertEquals(new BigDecimal("-10.00"), report.diferencaEmCaixa());
        assertEquals(new BigDecimal("220.00"), report.saldoEsperadoEmCaixa());
    }

    @Test
    @DisplayName("Deve rejeitar sessao de outra filial")
    void shouldRejectSessionFromAnotherCompany() {
        UUID sessionId = UUID.randomUUID();

        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.getSessionDetails(sessionId));
    }

    @Test
    @DisplayName("Deve rejeitar operador autorizador de outra filial")
    void shouldRejectAuthorizerFromAnotherCompany() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.empty());
        stubCashReceived(sessionId, new BigDecimal("80.00"));
        stubTolerance("0.00");

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest("SUP-001", new BigDecimal("170.00")))
        );
        assertTrue(session.hasDivergentCount());
    }

    @Test
    @DisplayName("Deve rejeitar operador autorizador sem nivel de supervisor")
    void shouldRejectAuthorizerBelowSupervisor() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        Operator authorizer = buildAuthorizer();
        authorizer.setPermissionLevel(PermissionLevel.BASIC);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.of(authorizer));
        stubCashReceived(sessionId, new BigDecimal("80.00"));
        stubTolerance("0.00");

        assertThrows(
                ForbiddenOperationException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest("SUP-001", new BigDecimal("170.00")))
        );
        verify(closingRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Deve bloquear fechamento quando ha venda paga nao concluida")
    void shouldBlockClosingWhenPaidSaleIsPending() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        Sale paidSale = buildPaidSale(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(saleRepository.findPendingBySessionId(sessionId)).thenReturn(List.of(paidSale));

        PendingSalesBlockClosingException exception = assertThrows(
                PendingSalesBlockClosingException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("180.00")))
        );

        assertEquals("CASH_REGISTER_PENDING_SALES", exception.getCode());
        verify(closingRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar automaticamente venda vazia criada pelo PDV e fechar normalmente")
    void shouldAutoCancelEmptyOpenSaleAndClose() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        Sale emptyOpenSale = Sale.createForSession(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(saleRepository.findPendingBySessionId(sessionId)).thenReturn(List.of(emptyOpenSale));
        stubSummaryOf(sessionId, 1L, 0L, new BigDecimal("80.00"));
        stubTolerance("0.00");

        CloseSessionResponse response = useCase.execute(
                sessionId,
                new CloseSessionRequest(null, new BigDecimal("180.00"))
        );

        assertEquals("CANCELED", emptyOpenSale.getStateName());
        verify(saleRepository).save(emptyOpenSale);
        assertNotNull(response);
        verify(closingRepository).save(any(CashRegisterClosing.class));
    }

    @Test
    @DisplayName("Deve fechar sem liberacao quando a diferenca e igual a tolerancia")
    void shouldCloseWithoutAuthorizationAtTheToleranceLimit() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        stubSummaryOf(sessionId, 1L, 0L, new BigDecimal("80.00"));
        stubTolerance("5.00");

        CloseSessionResponse response = useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("175.00")));

        assertEquals(0, response.resumo().diferencaEmCaixa().compareTo(new BigDecimal("-5.00")));
        assertFalse(session.hasDivergentCount());
    }

    @Test
    @DisplayName("Deve exigir liberacao, manter a sessao aberta e guardar a contagem acima da tolerancia")
    void shouldRequireAuthorizationAndKeepTheCountAboveTolerance() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        Sale emptyOpenSale = Sale.createForSession(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(saleRepository.findPendingBySessionId(sessionId)).thenReturn(List.of(emptyOpenSale));
        stubCashReceived(sessionId, new BigDecimal("80.00"));
        stubTolerance("5.00");

        assertThrows(
                ClosingAuthorizationRequiredException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("174.99")))
        );

        assertTrue(session.isOpen());
        assertEquals(new BigDecimal("174.99"), session.getFirstDivergentCountedAmount());
        assertEquals("OPEN", emptyOpenSale.getStateName());
        verify(closingRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir liberacao na recontagem mesmo com valor dentro da tolerancia")
    void shouldRequireAuthorizationOnRecountAfterDivergence() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        session.registerDivergentCount(new BigDecimal("150.00"));

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));

        assertThrows(
                ClosingAuthorizationRequiredException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("180.00")))
        );

        assertEquals(new BigDecimal("150.00"), session.getFirstDivergentCountedAmount());
    }

    @Test
    @DisplayName("Deve fechar acima da tolerancia com liberacao de supervisor")
    void shouldCloseAboveToleranceWithSupervisorAuthorization() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.of(buildAuthorizer()));
        stubSummaryOf(sessionId, 1L, 0L, new BigDecimal("80.00"));
        stubTolerance("0.00");

        CloseSessionResponse response = useCase.execute(sessionId, new CloseSessionRequest("SUP-001", new BigDecimal("170.00")));

        assertEquals("SUP-001", response.codigoOperadorAutorizador());
        assertEquals(0, response.resumo().diferencaEmCaixa().compareTo(new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Quem tem nivel de supervisor fecha a propria sessao com diferenca sem liberacao")
    void shouldLetSupervisorCloseOwnSessionWithDifference() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        session.getOperator().setPermissionLevel(PermissionLevel.SUPERVISOR);

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        stubSummaryOf(sessionId, 1L, 0L, new BigDecimal("80.00"));

        CloseSessionResponse response = useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("100.00")));

        assertNull(response.codigoOperadorAutorizador());
        assertEquals(0, response.resumo().diferencaEmCaixa().compareTo(new BigDecimal("-80.00")));
    }

    @Test
    @DisplayName("Sessao ja fechada recusa o fechamento antes da contagem")
    void shouldRejectClosedSessionBeforeCounting() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        session.close();

        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));

        assertThrows(
                SessionAlreadyClosedException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("100.00")))
        );
    }

    @Test
    @DisplayName("Outra conta nao fecha a sessao nem registra contagem")
    void shouldRejectClosingBySomeoneOtherThanTheOwner() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        session.getOperator().setPermissionLevel(PermissionLevel.SUPERVISOR);
        stubSignedInAccount(UUID.randomUUID());
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));

        SessionOwnedByAnotherAccountException exception = assertThrows(
                SessionOwnedByAnotherAccountException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest(null, new BigDecimal("10.00")))
        );

        assertEquals("SESSION_OWNED_BY_ANOTHER_ACCOUNT", exception.getCode());
        assertTrue(session.isOpen());
        assertFalse(session.hasDivergentCount());
        verify(closingRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Supervisor inativo nao libera o fechamento")
    void shouldRejectInactiveAuthorizer() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = sessionWithId(sessionId);
        Operator authorizer = buildAuthorizer();
        authorizer.setActive(false);
        stubOwnerSignedIn();
        when(sessionRepository.findForClosingByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.of(authorizer));
        stubCashReceived(sessionId, new BigDecimal("80.00"));
        stubTolerance("0.00");

        assertThrows(
                ForbiddenOperationException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest("SUP-001", new BigDecimal("170.00")))
        );
        assertTrue(session.isOpen());
        verify(closingRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Operador nao ve os totais da sessao enquanto ela esta aberta")
    void shouldHideLiveTotalsFromOperatorWhileSessionIsOpen() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(buildOpenSession()));
        when(authenticatedAccountProvider.require()).thenReturn(accountWithRole(AccountRole.OPERATOR));

        assertThrows(ForbiddenOperationException.class, () -> useCase.getSessionDetails(sessionId));
    }

    @Test
    @DisplayName("Operador ve a diferenca depois de fechar")
    void shouldShowDifferenceToOperatorAfterClosing() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        session.close();
        SessionSummaryResponse summary = new SessionSummaryResponse(
                0,
                0,
                BigDecimal.ZERO,
                java.util.Map.of(),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("97.00"),
                new BigDecimal("-3.00")
        );
        CashRegisterClosing closing = CashRegisterClosing.create(session, null, summary, new BigDecimal("97.00"));
        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(closingRepository.findBySession(session)).thenReturn(Optional.of(closing));

        CloseSessionResponse response = useCase.getSessionDetails(sessionId);

        assertEquals(0, response.resumo().diferencaEmCaixa().compareTo(new BigDecimal("-3.00")));
    }

    @Test
    @DisplayName("Administrador ve os totais da sessao aberta")
    void shouldShowLiveTotalsToAdministrator() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(buildOpenSession()));
        when(authenticatedAccountProvider.require()).thenReturn(accountWithRole(AccountRole.ADMIN));

        assertEquals(new BigDecimal("100.00"), useCase.getSessionDetails(sessionId).resumo().saldoEsperadoEmCaixa());
    }

    private CashRegisterSession sessionWithId(UUID sessionId) {
        CashRegisterSession session = org.mockito.Mockito.spy(buildOpenSession());
        org.mockito.Mockito.doReturn(sessionId).when(session).getId();
        return session;
    }

    private void stubTolerance(String amount) {
        when(companyService.cashClosingTolerance(COMPANY_ID)).thenReturn(new BigDecimal(amount));
    }

    private Account accountWithRole(AccountRole role) {
        return new Account(UUID.randomUUID(), "Pessoa", "pessoa@loja.com", "hash", role);
    }

    private void stubCashReceived(UUID sessionId, BigDecimal totalPaidInCash) {
        when(saleRepository.sumCompletedPaymentsByMethod(sessionId))
                .thenReturn(List.of(new SessionPaymentMethodTotal(PaymentMethod.CASH, totalPaidInCash)));
    }

    private void stubSummaryOf(UUID sessionId, long completed, long canceled, BigDecimal totalPaidInCash) {
        when(saleRepository.countCompletedBySessionId(sessionId)).thenReturn(completed);
        when(saleRepository.countCanceledBySessionId(sessionId)).thenReturn(canceled);
        when(saleRepository.sumCompletedTotalBySessionId(sessionId)).thenReturn(totalPaidInCash);
        when(saleRepository.sumCompletedPaymentsByMethod(sessionId))
                .thenReturn(List.of(new SessionPaymentMethodTotal(PaymentMethod.CASH, totalPaidInCash)));
    }

    private Sale buildPaidSale(UUID sessionId) {
        Sale sale = Sale.createForSession(sessionId);
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Produto Teste");
        sale.addItem(product, new BigDecimal("80.00"));
        sale.startPayment();
        sale.setPayments(new LinkedHashSet<>(List.of(
                new Payment(sale, PaymentMethod.CASH, new BigDecimal("80.00"), BigDecimal.ZERO, null, true)
        )));
        sale.finishPayment();
        return sale;
    }

    private CashRegisterSession buildOpenSession() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", COMPANY_ID);
        Operator operator = new Operator("Operador", "OP-001");
        operator.setAccountId(OWNER_ACCOUNT_ID);
        return CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
    }

    private void stubOwnerSignedIn() {
        stubSignedInAccount(OWNER_ACCOUNT_ID);
    }

    private void stubSignedInAccount(UUID accountId) {
        Account account = accountWithRole(AccountRole.OPERATOR);
        account.setId(accountId);
        when(authenticatedAccountProvider.require()).thenReturn(account);
    }

    private Operator buildAuthorizer() {
        Operator operator = new Operator("Supervisor", "SUP-001");
        operator.setCompanyId(COMPANY_ID);
        operator.setPermissionLevel(PermissionLevel.SUPERVISOR);
        return operator;
    }
}
