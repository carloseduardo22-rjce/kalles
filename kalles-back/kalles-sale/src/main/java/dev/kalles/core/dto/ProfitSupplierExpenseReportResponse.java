package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProfitSupplierExpenseReportResponse(
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalSales,
    BigDecimal totalSupplierExpenses,
    BigDecimal estimatedProfit,
    BigDecimal marginPercentage,
    LocalDateTime generatedAt,
    List<SupplierExpenseProductItemResponse> purchasedProducts
) {}
