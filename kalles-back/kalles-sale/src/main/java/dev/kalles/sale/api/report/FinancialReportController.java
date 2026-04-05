package dev.kalles.sale.api.report;

import dev.kalles.sale.core.dto.ProfitSupplierExpenseReportResponse;
import dev.kalles.sale.core.service.FinancialReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Relatorios Financeiros", description = "Relatorios consolidados de vendas e gastos com fornecedores")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    @GetMapping("/profit-vs-supplier-expenses")
    @Operation(summary = "Relatorio de lucro x gastos com fornecedores")
    public ResponseEntity<ProfitSupplierExpenseReportResponse> getProfitVsSupplierExpenses(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(financialReportService.getProfitVsSupplierExpenses(startDate, endDate));
    }
}
