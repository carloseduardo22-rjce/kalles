package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CloseSessionRequest;
import dev.kalles.cashregister.dto.CloseSessionResponse;
import dev.kalles.cashregister.dto.SessionSummaryResponse;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterClosing;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.CashRegisterClosingRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.core.entity.Payment;
import dev.kalles.core.entity.Product;
import dev.kalles.core.entity.Sale;
import dev.kalles.core.enums.operator.PermissionLevel;
import dev.kalles.core.enums.payment.PaymentMethod;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.SaleRepository;
import dev.kalles.core.state.CompletedState;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloseSessionUseCase - Caso de uso de fechamento")
class CloseSessionUseCaseTest {

    private static final UUID COMPANY_ID = UUID.fromString("99f449b5-3f12-48f6-b4a7-dfa165ed39d7");

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    @Mock
    private CashRegisterClosingRepository closingRepository;

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private SaleRepository saleRepository;

    private CloseSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        useCase = new CloseSessionUseCase(
                sessionRepository,
                closingRepository,
                operatorRepository,
                saleRepository
        );
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve fechar sessao persistindo o snapshot do fechamento")
    void shouldCloseSessionPersistingClosingSnapshot() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = org.mockito.Mockito.spy(buildOpenSession());
        org.mockito.Mockito.doReturn(sessionId).when(session).getId();
        Operator authorizer = buildAuthorizer();
        Sale completedSale = buildCompletedSale(sessionId.toString());

        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.of(authorizer));
        when(saleRepository.findAllBySessionTokenAndStateIn(any(), any()))
                .thenReturn(List.of(completedSale), List.of());

        CloseSessionResponse response = useCase.execute(
                sessionId,
                new CloseSessionRequest("SUP-001", new BigDecimal("180.00"))
        );

        ArgumentCaptor<CashRegisterClosing> closingCaptor = ArgumentCaptor.forClass(CashRegisterClosing.class);
        verify(closingRepository).save(closingCaptor.capture());

        CashRegisterClosing savedClosing = closingCaptor.getValue();
        assertEquals(new BigDecimal("80.00"), savedClosing.getCashSalesAmount());
        assertEquals(new BigDecimal("180.00"), savedClosing.getExpectedCashAmount());
        assertEquals(new BigDecimal("180.00"), savedClosing.getCountedCashAmount());
        assertEquals(0, savedClosing.getCashDifferenceAmount().compareTo(BigDecimal.ZERO));
        assertNotNull(response.nomeOperadorAutorizador());
        assertTrue(!response.nomeOperadorAutorizador().isBlank());
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
        CashRegisterSession session = buildOpenSession();

        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest("SUP-001", new BigDecimal("180.00")))
        );
    }

    @Test
    @DisplayName("Deve bloquear fechamento quando ha venda paga nao concluida")
    void shouldBlockClosingWhenPaidSaleIsPending() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = org.mockito.Mockito.spy(buildOpenSession());
        org.mockito.Mockito.doReturn(sessionId).when(session).getId();
        Operator authorizer = buildAuthorizer();
        Sale paidSale = buildPaidSale(sessionId.toString());

        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.of(authorizer));
        when(saleRepository.findPendingBySessionToken(sessionId.toString())).thenReturn(List.of(paidSale));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(sessionId, new CloseSessionRequest("SUP-001", new BigDecimal("180.00")))
        );

        assertTrue(exception.getMessage().contains("venda(s) pendente(s)"));
        verify(closingRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar automaticamente venda vazia criada pelo PDV e fechar normalmente")
    void shouldAutoCancelEmptyOpenSaleAndClose() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = org.mockito.Mockito.spy(buildOpenSession());
        org.mockito.Mockito.doReturn(sessionId).when(session).getId();
        Operator authorizer = buildAuthorizer();
        Sale emptyOpenSale = Sale.createForSession(sessionId.toString());
        Sale completedSale = buildCompletedSale(sessionId.toString());

        when(sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, COMPANY_ID)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCodeAndCompanyId("SUP-001", COMPANY_ID)).thenReturn(Optional.of(authorizer));
        when(saleRepository.findPendingBySessionToken(sessionId.toString())).thenReturn(List.of(emptyOpenSale));
        when(saleRepository.findAllBySessionTokenAndStateIn(any(), any()))
                .thenReturn(List.of(completedSale), List.of());

        CloseSessionResponse response = useCase.execute(
                sessionId,
                new CloseSessionRequest("SUP-001", new BigDecimal("180.00"))
        );

        assertEquals("CANCELED", emptyOpenSale.getStateName());
        verify(saleRepository).save(emptyOpenSale);
        assertNotNull(response);
        verify(closingRepository).save(any(CashRegisterClosing.class));
    }

    private Sale buildPaidSale(String sessionToken) {
        Sale sale = Sale.createForSession(sessionToken);
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
        return CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
    }

    private Operator buildAuthorizer() {
        Operator operator = new Operator("Supervisor", "SUP-001");
        operator.setCompanyId(COMPANY_ID);
        operator.setPermissionLevel(PermissionLevel.SUPERVISOR);
        return operator;
    }

    private Sale buildCompletedSale(String sessionToken) {
        Sale sale = Sale.createForSession(sessionToken);
        sale.setTotal(new BigDecimal("80.00"));
        sale.setPayments(new LinkedHashSet<>(List.of(
                new Payment(sale, PaymentMethod.CASH, new BigDecimal("80.00"), BigDecimal.ZERO, null, true)
        )));
        sale.setState(new CompletedState());
        return sale;
    }
}
