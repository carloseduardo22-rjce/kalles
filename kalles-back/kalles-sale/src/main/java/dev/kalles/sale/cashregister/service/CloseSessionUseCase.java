package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CloseSessionRequest;
import dev.kalles.sale.cashregister.dto.CloseSessionResponse;
import dev.kalles.sale.cashregister.dto.SessionSummaryResponse;
import dev.kalles.sale.cashregister.entity.CashRegisterClosing;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.CashRegisterClosingRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import dev.kalles.sale.core.entity.Payment;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.state.CanceledState;
import dev.kalles.sale.core.state.CompletedState;
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
                .findBySessionPeriod_OpenedAtBetweenOrderBySessionPeriod_OpenedAtDesc(start, end)
                .stream()
                .map(this::toSessionDetailsResponse)
                .toList();
    }

    private CashRegisterSession findSessionOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Sessão de caixa não encontrada: " + sessionId));
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
        Operator operator = operatorRepository.findByCode(operatorCode)
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
