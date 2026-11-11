package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CloseSessionRequest;
import dev.kalles.cashregister.dto.CloseSessionResponse;
import dev.kalles.cashregister.dto.SessionSummaryResponse;
import dev.kalles.cashregister.entity.CashRegisterClosing;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.ClosingAuthorizationRequiredException;
import dev.kalles.cashregister.exception.ClosingAuthorizerNotAllowedException;
import dev.kalles.cashregister.exception.ClosingAuthorizerNotFoundException;
import dev.kalles.cashregister.exception.PendingSalesBlockClosingException;
import dev.kalles.cashregister.exception.SessionOwnedByAnotherAccountException;
import dev.kalles.cashregister.repository.CashRegisterClosingRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.company.service.CompanyService;
import dev.kalles.sale.dto.SessionPaymentMethodTotal;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.sale.state.OpenState;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.service.AuthenticatedAccountProvider;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
    private final PairedDeviceSessionGuard pairedDeviceSessionGuard;
    private final CompanyService companyService;
    private final PermissionService permissionService;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    @Transactional(noRollbackFor = {
            ClosingAuthorizationRequiredException.class,
            ClosingAuthorizerNotFoundException.class,
            ClosingAuthorizerNotAllowedException.class
    })
    public CloseSessionResponse execute(UUID sessionId, CloseSessionRequest request) {
        CashRegisterSession session = sessionRepository
                .findForClosingByIdAndCashRegister_CompanyId(sessionId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> sessionNotFound(sessionId));
        pairedDeviceSessionGuard.ensureCanOperate(session.getCashRegister());
        ensureClosedByOwner(session);
        session.ensureOpen();
        List<Sale> pendingSales = saleRepository.findPendingBySessionId(session.getId());
        ensureNoBlockingSales(pendingSales);
        Operator authorizedByOperator = resolveAuthorizer(session, request);
        cancelIdleSales(pendingSales);
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
        ensureCanSeeExpectedCash(session);
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
                .orElseThrow(() -> sessionNotFound(sessionId));
    }

    private NotFoundException sessionNotFound(UUID sessionId) {
        return new NotFoundException("Sessao de caixa nao encontrada: " + sessionId);
    }

    private void ensureClosedByOwner(CashRegisterSession session) {
        UUID ownerAccountId = session.getOperator().getAccountId();
        if (ownerAccountId == null || !ownerAccountId.equals(authenticatedAccountProvider.require().getId())) {
            throw new SessionOwnedByAnotherAccountException();
        }
    }

    private void ensureCanSeeExpectedCash(CashRegisterSession session) {
        if (session.isOpen() && authenticatedAccountProvider.require().getRole() != AccountRole.ADMIN) {
            throw new ForbiddenOperationException("O valor esperado fica oculto enquanto a sessão está aberta.");
        }
    }

    private void ensureNoBlockingSales(List<Sale> pendingSales) {
        long blockingSales = pendingSales.stream().filter(sale -> !isIdle(sale)).count();
        if (blockingSales > 0) {
            throw new PendingSalesBlockClosingException(blockingSales);
        }
    }

    private void cancelIdleSales(List<Sale> pendingSales) {
        for (Sale sale : pendingSales) {
            sale.cancel();
            saleRepository.save(sale);
        }
    }

    private boolean isIdle(Sale sale) {
        return OpenState.NAME.equals(sale.getStateName())
                && sale.getItems().isEmpty()
                && sale.getPayments().isEmpty();
    }

    private Operator resolveAuthorizer(CashRegisterSession session, CloseSessionRequest request) {
        if (permissionService.canCloseWithDifference(session.getOperator())) {
            return null;
        }
        if (!session.hasDivergentCount() && isWithinTolerance(session, request.countedCashAmount())) {
            return null;
        }
        session.registerDivergentCount(request.countedCashAmount());
        if (request.authorizedOperatorCode() == null || request.authorizedOperatorCode().isBlank()) {
            throw new ClosingAuthorizationRequiredException();
        }
        return findAuthorizedOperator(request.authorizedOperatorCode());
    }

    private boolean isWithinTolerance(CashRegisterSession session, BigDecimal countedAmount) {
        BigDecimal expectedCash = session.getInitialAmountValue().add(cashReceived(session.getId()));
        BigDecimal difference = countedAmount.subtract(expectedCash).abs();
        return difference.compareTo(companyService.cashClosingTolerance(CompanyContextHolder.requireCompanyId())) <= 0;
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
                .orElseThrow(() -> new ClosingAuthorizerNotFoundException(operatorCode));

        if (!operator.isActive() || !permissionService.canCloseWithDifference(operator)) {
            throw new ClosingAuthorizerNotAllowedException();
        }

        return operator;
    }

    private BigDecimal cashReceived(UUID sessionId) {
        return saleRepository.sumCompletedPaymentsByMethod(sessionId).stream()
                .filter(total -> total.method() == PaymentMethod.CASH)
                .map(SessionPaymentMethodTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private SessionSummaryResponse buildLiveSummary(CashRegisterSession session) {
        return computeSummary(session.getId(), session.getInitialAmountValue());
    }

    private SessionSummaryResponse computeSummary(UUID sessionId, BigDecimal initialAmount) {
        long completedSales = saleRepository.countCompletedBySessionId(sessionId);
        long canceledSales = saleRepository.countCanceledBySessionId(sessionId);
        BigDecimal totalVendido = saleRepository.sumCompletedTotalBySessionId(sessionId);

        Map<String, BigDecimal> totalPorMetodo = saleRepository.sumCompletedPaymentsByMethod(sessionId)
                .stream()
                .collect(Collectors.toMap(
                        total -> total.method().name(),
                        SessionPaymentMethodTotal::total,
                        BigDecimal::add,
                        LinkedHashMap::new
                ));

        BigDecimal totalEmDinheiro = totalPorMetodo.getOrDefault(PaymentMethod.CASH.name(), BigDecimal.ZERO);
        BigDecimal saldoEsperadoEmCaixa = initialAmount.add(totalEmDinheiro);

        return new SessionSummaryResponse(
                Math.toIntExact(completedSales),
                Math.toIntExact(canceledSales),
                totalVendido,
                totalPorMetodo,
                totalEmDinheiro,
                saldoEsperadoEmCaixa,
                null,
                null
        );
    }
}
