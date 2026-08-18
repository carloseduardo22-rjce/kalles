package dev.kalles.report.service;

import dev.kalles.inventory.dto.SupplierExpenseProductSummary;
import dev.kalles.inventory.repository.StockEntryRepository;
import dev.kalles.report.dto.ProfitSupplierExpenseReportResponse;
import dev.kalles.report.service.FinancialReportService;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.exception.CompanyContextRequiredException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialReportService - Servico de Relatorio Financeiro")
class FinancialReportServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("c7b9be77-2eb2-4a4e-8ae6-0b17f77a1201");

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private StockEntryRepository stockEntryRepository;

    @InjectMocks
    private FinancialReportService financialReportService;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve consolidar vendas, gastos e margem da filial ativa")
    void shouldAggregateSalesExpensesAndMarginForActiveCompany() {
        LocalDate startDate = LocalDate.of(2026, 4, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 30);

        when(saleRepository.sumCompletedTotalsBetween(COMPANY_ID, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()))
                .thenReturn(new BigDecimal("1000.00"));
        when(stockEntryRepository.sumTotalCostBetween(COMPANY_ID, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()))
                .thenReturn(new BigDecimal("400.00"));
        when(stockEntryRepository.summarizeByProductBetween(COMPANY_ID, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(new SupplierExpenseProductSummary(
                        UUID.randomUUID(),
                        "Arroz",
                        "ARZ-001",
                        20L,
                        new BigDecimal("400.00")
                )));

        ProfitSupplierExpenseReportResponse response = financialReportService.getProfitVsSupplierExpenses(startDate, endDate);

        assertEquals(new BigDecimal("1000.00"), response.totalSales());
        assertEquals(new BigDecimal("400.00"), response.totalSupplierExpenses());
        assertEquals(new BigDecimal("600.00"), response.estimatedProfit());
        assertEquals(new BigDecimal("60.00"), response.marginPercentage());
        assertEquals(1, response.purchasedProducts().size());
        assertEquals(new BigDecimal("20.00"), response.purchasedProducts().get(0).averageUnitCost());
    }

    @Test
    @DisplayName("Deve rejeitar intervalo com data final anterior a inicial")
    void shouldRejectWhenEndDateIsBeforeStartDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                financialReportService.getProfitVsSupplierExpenses(
                        LocalDate.of(2026, 4, 30),
                        LocalDate.of(2026, 4, 1)
                ));

        assertEquals("A data final nao pode ser menor que a data inicial.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve exigir filial ativa no contexto")
    void shouldRequireCompanyContext() {
        CompanyContextHolder.clear();

        assertThrows(CompanyContextRequiredException.class, () ->
                financialReportService.getProfitVsSupplierExpenses(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 30)
                ));
    }
}
