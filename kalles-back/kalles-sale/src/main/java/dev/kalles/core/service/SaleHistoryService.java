package dev.kalles.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kalles.core.dto.SaleHistoryResponse;
import dev.kalles.core.entity.Sale;
import dev.kalles.core.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleHistoryService {

    private static final Set<String> ALLOWED_STATES = Set.of(
            "OPEN",
            "ON_HOLD",
            "PAYMENT_IN_PROGRESS",
            "PAID",
            "COMPLETED",
            "CANCELED"
    );

    private final SaleRepository saleRepository;
    private final SaleHistoryExcelExporter excelExporter;

    @Transactional(readOnly = true)
    public List<SaleHistoryResponse> list(LocalDate startDate, LocalDate endDate, String state) {
        SearchPeriod period = validatePeriod(startDate, endDate);
        String normalizedState = normalizeState(state);
        UUID companyId = getCompanyId();

        List<SaleRepository.SaleHistoryRow> rows = normalizedState == null
                ? saleRepository.findHistoryRows(companyId, period.start(), period.endExclusive())
                : saleRepository.findHistoryRowsByState(companyId, period.start(), period.endExclusive(), normalizedState);

        if (rows.isEmpty()) {
            return List.of();
        }

        Map<UUID, LocalDateTime> openedAtBySaleId = rows.stream()
                .collect(Collectors.toMap(
                        row -> UUID.fromString(row.getId()),
                        SaleRepository.SaleHistoryRow::getOpenedAt,
                        (first, ignored) -> first
                ));

        Map<UUID, Sale> salesById = saleRepository.findAllWithDetailsByIdIn(rows.stream()
                        .map(row -> UUID.fromString(row.getId()))
                        .toList())
                .stream()
                .collect(Collectors.toMap(Sale::getId, Function.identity()));

        return rows.stream()
                .map(row -> salesById.get(UUID.fromString(row.getId())))
                .filter(sale -> sale != null)
                .sorted(Comparator
                        .comparing((Sale sale) -> openedAtBySaleId.get(sale.getId())).reversed()
                        .thenComparing(sale -> sale.getId().toString()))
                .map(sale -> SaleHistoryResponse.from(sale, openedAtBySaleId.get(sale.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] export(LocalDate startDate, LocalDate endDate, String state) {
        return excelExporter.export(list(startDate, endDate, state));
    }

    private SearchPeriod validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("A data inicial e obrigatoria.");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("A data final e obrigatoria.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("A data final nao pode ser menor que a data inicial.");
        }
        return new SearchPeriod(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }

        String normalized = state.trim().toUpperCase();
        if (!ALLOWED_STATES.contains(normalized)) {
            throw new IllegalArgumentException("Estado de venda invalido: " + state);
        }
        return normalized;
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }

    private record SearchPeriod(LocalDateTime start, LocalDateTime endExclusive) {
    }
}
