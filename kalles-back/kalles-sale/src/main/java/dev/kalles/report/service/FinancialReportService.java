package dev.kalles.report.service;

import dev.kalles.core.repository.SaleRepository;
import dev.kalles.inventory.dto.SupplierExpenseProductSummary;
import dev.kalles.inventory.repository.StockEntryRepository;
import dev.kalles.report.dto.ProfitSupplierExpenseReportResponse;
import dev.kalles.report.dto.SupplierExpenseProductItemResponse;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final SaleRepository saleRepository;
    private final StockEntryRepository stockEntryRepository;

    @Transactional(readOnly = true)
    public ProfitSupplierExpenseReportResponse getProfitVsSupplierExpenses(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("A data final nao pode ser menor que a data inicial.");
        }

        UUID companyId = getCompanyId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        BigDecimal totalSales = saleRepository.sumCompletedTotalsBetween(companyId, start, end);
        BigDecimal totalSupplierExpenses = stockEntryRepository.sumTotalCostBetween(companyId, start, end);
        BigDecimal estimatedProfit = totalSales.subtract(totalSupplierExpenses);
        BigDecimal marginPercentage = totalSales.compareTo(BigDecimal.ZERO) > 0
            ? estimatedProfit.multiply(BigDecimal.valueOf(100)).divide(totalSales, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        List<SupplierExpenseProductItemResponse> purchasedProducts = stockEntryRepository
            .summarizeByProductBetween(companyId, start, end)
            .stream()
            .map(this::toProductItem)
            .toList();

        return new ProfitSupplierExpenseReportResponse(
            startDate,
            endDate,
            totalSales,
            totalSupplierExpenses,
            estimatedProfit,
            marginPercentage,
            LocalDateTime.now(),
            purchasedProducts
        );
    }

    private SupplierExpenseProductItemResponse toProductItem(SupplierExpenseProductSummary summary) {
        BigDecimal averageUnitCost = summary.totalQuantity() != null && summary.totalQuantity() > 0
            ? summary.totalCost().divide(BigDecimal.valueOf(summary.totalQuantity()), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new SupplierExpenseProductItemResponse(
            summary.productId(),
            summary.productName(),
            summary.productInternalCode(),
            summary.totalQuantity() != null ? summary.totalQuantity() : 0,
            averageUnitCost,
            summary.totalCost()
        );
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }
}
