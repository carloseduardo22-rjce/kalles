package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CloseSessionRequest;
import dev.kalles.sale.cashregister.dto.CloseSessionResponse;
import dev.kalles.sale.cashregister.dto.SessionSummaryResponse;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterClosing;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.CashRegisterClosingRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.entity.Payment;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.state.CompletedState;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloseSessionUseCase - Caso de uso de fechamento")
class CloseSessionUseCaseTest {

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
        useCase = new CloseSessionUseCase(
                sessionRepository,
                closingRepository,
                operatorRepository,
                saleRepository
        );
    }

    @Test
    @DisplayName("Deve fechar sessao persistindo o snapshot do fechamento")
    void shouldCloseSessionPersistingClosingSnapshot() {
        UUID sessionId = UUID.randomUUID();
        CashRegisterSession session = org.mockito.Mockito.spy(buildOpenSession());
        org.mockito.Mockito.doReturn(sessionId).when(session).getId();
        Operator authorizer = buildAuthorizer();
        Sale completedSale = buildCompletedSale(sessionId.toString());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(operatorRepository.findByCode("SUP-001")).thenReturn(Optional.of(authorizer));
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
        assertEquals(BigDecimal.ZERO, savedClosing.getCashDifferenceAmount());
        assertNotNull(response.nomeOperadorAutorizador());
        assertTrue(!response.nomeOperadorAutorizador().isBlank());
        assertEquals(new BigDecimal("180.00"), response.resumo().saldoEsperadoEmCaixa());
        assertEquals(BigDecimal.ZERO, response.resumo().diferencaEmCaixa());
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

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(closingRepository.findBySession(session)).thenReturn(Optional.of(closing));

        SessionSummaryResponse report = useCase.getReport(sessionId);

        assertEquals(new BigDecimal("210.00"), report.valorInformadoEmCaixa());
        assertEquals(new BigDecimal("-10.00"), report.diferencaEmCaixa());
        assertEquals(new BigDecimal("220.00"), report.saldoEsperadoEmCaixa());
    }

    private CashRegisterSession buildOpenSession() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("Operador", "OP-001");
        return CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
    }

    private Operator buildAuthorizer() {
        Operator operator = new Operator("Supervisor", "SUP-001");
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
