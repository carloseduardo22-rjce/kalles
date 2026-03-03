package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CloseSessionResponse;
import dev.kalles.sale.cashregister.dto.SessionSummaryResponse;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloseSessionUseCase {

    private final CashRegisterSessionRepository sessionRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public CloseSessionResponse execute(UUID sessionId) {
        CashRegisterSession session = findSessionOrThrow(sessionId);

        session.close(); 

        SessionSummaryResponse summary = computeSummary(sessionId.toString());

        sessionRepository.save(session);

        return CloseSessionResponse.fromEntity(session, summary);
    }

    @Transactional(readOnly = true)
    public SessionSummaryResponse getReport(UUID sessionId) {
        findSessionOrThrow(sessionId);
        return computeSummary(sessionId.toString());
    }

    private CashRegisterSession findSessionOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Sessão de caixa não encontrada: " + sessionId));
    }

    private SessionSummaryResponse computeSummary(String sessionToken) {
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

        return new SessionSummaryResponse(
                completedSales.size(),
                canceledSales.size(),
                totalVendido,
                totalPorMetodo
        );
    }
}
