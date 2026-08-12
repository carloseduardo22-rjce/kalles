package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CloseSessionRequest;
import dev.kalles.cashregister.dto.CloseSessionResponse;
import dev.kalles.cashregister.dto.SessionSummaryResponse;
import dev.kalles.cashregister.entity.CashRegisterClosing;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.repository.CashRegisterClosingRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.sale.state.CanceledState;
import dev.kalles.sale.state.CompletedState;
import dev.kalles.sale.state.OpenState;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloseSessionUseCase {

    private final CashRegisterSessionRepository sessionRepository;
    private final CashRegisterClosingRepository closingRepository;
    private final OperatorRepository operatorRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public CloseSessionResponse execute(UUID sessionId, CloseSessionRequest request) {
        CashRegisterSession session = findSessionOrThrow(sessionId);
        Operator authorizedByOperator = findAuthorizedOperator(request.authorizedOperatorCode());
        resolvePendingSales(session);
        SessionSummaryResponse summary = buildLiveSummary(session);

        session.close();
        CashRegisterClosing closing = CashRegisterClosing.create(
                session,
                authorizedByOperator,
                summary,
                request.countedCashAmount()
        );

        sessionRepository.save(session);
        closingRepository.save(closing);

        return CloseSessionResponse.fromEntity(session, closing.toSummaryResponse(), authorizedByOperator);
    }

    @Transactional(readOnly = true)
    public SessionSummaryResponse getReport(UUID sessionId) {
        CashRegisterSession session = findSessionOrThrow(sessionId);
        return closingRepository.findBySession(session)
                .map(CashRegisterClosing::toSummaryResponse)
                .orElseGet(() -> buildLiveSummary(session));
    }

    @Transactional(readOnly = true)
    public CloseSessionResponse getSessionDetails(UUID sessionId) {
        CashRegisterSession session = findSessionOrThrow(sessionId);
        return toSessionDetailsResponse(session);
    }

    @Transactional(readOnly = true)
    public List<CloseSessionResponse> listSessionsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        return sessionRepository
                .findByCashRegister_CompanyIdAndSessionPeriod_OpenedAtBetweenOrderBySessionPeriod_OpenedAtDesc(
                        CompanyContextHolder.requireCompanyId(),
                        start,
                        end
                )
                .stream()
                .map(this::toSessionDetailsResponse)
                .toList();
    }

    private CashRegisterSession findSessionOrThrow(UUID sessionId) {
        return sessionRepository.findByIdAndCashRegister_CompanyId(sessionId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Sessao de caixa nao encontrada: " + sessionId));
    }

    /**
     * Impede o fechamento com vendas pendentes: uma venda PAID fora do fechamento
     * significa dinheiro recebido que não entraria no caixa. Vendas vazias criadas
     * automaticamente pelo PDV entre atendimentos são canceladas em silêncio.
     */
    private void resolvePendingSales(CashRegisterSession session) {
        List<Sale> pendingSales = saleRepository.findPendingBySessionToken(session.getId().toString());

        long blockingSales = 0;
        for (Sale sale : pendingSales) {
            boolean idleState = OpenState.NAME.equals(sale.getStateName());
            boolean emptySale = sale.getItems().isEmpty() && sale.getPayments().isEmpty();

            if (idleState && emptySale) {
                sale.cancel();
                saleRepository.save(sale);
            } else {
                blockingSales++;
            }
        }

        if (blockingSales > 0) {
            throw new IllegalStateException(
                    "Nao e possivel fechar o caixa: existem " + blockingSales
                            + " venda(s) pendente(s) (em andamento, em pagamento ou pagas sem conclusao)."
                            + " Conclua ou cancele as vendas antes de fechar a sessao.");
        }
    }

    private CloseSessionResponse toSessionDetailsResponse(CashRegisterSession session) {
        return closingRepository.findBySession(session)
                .map(closing -> CloseSessionResponse.fromEntity(
                        session,
                        closing.toSummaryResponse(),
                        closing.getAuthorizedByOperator()
                ))
                .orElseGet(() -> CloseSessionResponse.fromEntity(session, buildLiveSummary(session)));
    }

    private Operator findAuthorizedOperator(String operatorCode) {
        Operator operator = operatorRepository.findByCodeAndCompanyId(operatorCode, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Operador autorizador nao encontrado: " + operatorCode));

        PermissionLevel permissionLevel = operator.getPermissionLevel();
        if (permissionLevel == null || permissionLevel.getLevel() < PermissionLevel.SUPERVISOR.getLevel()) {
            throw new IllegalArgumentException("Operador sem permissao para autorizar fechamento de caixa.");
        }

        return operator;
    }


    private SessionSummaryResponse buildLiveSummary(CashRegisterSession session) {
        return computeSummary(session.getId().toString(), session.getInitialAmountValue());
    }

    private SessionSummaryResponse computeSummary(String sessionToken, BigDecimal initialAmount) {
        List<Sale> completedSales = saleRepository.findAllBySessionTokenAndStateIn(
                sessionToken, List.of(new CompletedState()));
        List<Sale> canceledSales = saleRepository.findAllBySessionTokenAndStateIn(
                sessionToken, List.of(new CanceledState()));

        BigDecimal totalVendido = completedSales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> totalPorMetodo = completedSales.stream()
                .flatMap(s -> s.getPayments().stream())
                .filter(Payment::isConfirmed)
                .collect(Collectors.groupingBy(
                        p -> p.getMethod().name(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                p -> p.getAmount().subtract(p.getChangeAmount()),
                                BigDecimal::add)
                ));

        BigDecimal totalEmDinheiro = totalPorMetodo.getOrDefault("CASH", BigDecimal.ZERO);
        BigDecimal saldoEsperadoEmCaixa = initialAmount.add(totalEmDinheiro);

        return new SessionSummaryResponse(
                completedSales.size(),
                canceledSales.size(),
                totalVendido,
                totalPorMetodo,
                totalEmDinheiro,
                saldoEsperadoEmCaixa,
                null,
                null
        );
    }
}
