package dev.kalles.sale.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.sale.state.CompletedState;
import dev.kalles.security.context.CompanyContextHolder;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SaleHistoryServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174999");

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleHistoryExcelExporter excelExporter;

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    void shouldListSalesWithDetailsForActiveCompanyAndPeriod() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        UUID saleId = UUID.randomUUID();
        LocalDateTime openedAt = LocalDateTime.of(2026, 4, 20, 10, 0);
        Sale sale = completedSale(saleId, "session-token");

        when(saleRepository.findHistoryRows(
                eq(COMPANY_ID),
                eq(LocalDateTime.of(2026, 4, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 5, 1, 0, 0))
        )).thenReturn(List.of(new TestSaleHistoryRow(saleId.toString(), openedAt)));
        when(saleRepository.findAllByIdIn(List.of(saleId))).thenReturn(List.of(sale));

        SaleHistoryService service = new SaleHistoryService(saleRepository, excelExporter);

        var response = service.list(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), null);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(saleId);
        assertThat(response.getFirst().companyId()).isEqualTo(COMPANY_ID);
        assertThat(response.getFirst().state()).isEqualTo("COMPLETED");
        assertThat(response.getFirst().openedAt()).isEqualTo(openedAt);
        assertThat(response.getFirst().items()).hasSize(1);
    }

    @Test
    void shouldFilterByStateWhenStateIsProvided() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        SaleHistoryService service = new SaleHistoryService(saleRepository, excelExporter);

        service.list(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "completed");

        verify(saleRepository).findHistoryRowsByState(
                eq(COMPANY_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq("COMPLETED")
        );
    }

    @Test
    void shouldRejectInvalidPeriod() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        SaleHistoryService service = new SaleHistoryService(saleRepository, excelExporter);

        assertThatThrownBy(() -> service.list(LocalDate.of(2026, 4, 30), LocalDate.of(2026, 4, 1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data final nao pode ser menor que a data inicial.");
    }

    @Test
    void shouldExportListedSales() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        UUID saleId = UUID.randomUUID();
        Sale sale = completedSale(saleId, "session-token");
        when(saleRepository.findHistoryRows(any(), any(), any()))
                .thenReturn(List.of(new TestSaleHistoryRow(saleId.toString(), LocalDateTime.of(2026, 4, 20, 10, 0))));
        when(saleRepository.findAllByIdIn(List.of(saleId))).thenReturn(List.of(sale));
        when(excelExporter.export(any())).thenReturn(new byte[] {1, 2, 3});

        SaleHistoryService service = new SaleHistoryService(saleRepository, excelExporter);

        byte[] exported = service.export(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), null);

        assertThat(exported).containsExactly(1, 2, 3);
        verify(excelExporter).export(any());
    }

    private Sale completedSale(UUID saleId, String sessionToken) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Produto PDV");
        product.setInternalCode("SKU-001");

        Sale sale = new Sale();
        sale.setId(saleId);
        sale.setSessionToken(sessionToken);
        sale.setCompanyId(COMPANY_ID);
        sale.setSubtotal(new BigDecimal("30.00"));
        sale.setTotal(new BigDecimal("30.00"));
        sale.setAmountDue(BigDecimal.ZERO);
        sale.addItem(product, new BigDecimal("30.00"));
        sale.setState(new CompletedState());
        return sale;
    }

    private record TestSaleHistoryRow(String id, LocalDateTime openedAt) implements SaleRepository.SaleHistoryRow {
        @Override
        public String getId() {
            return id;
        }

        @Override
        public LocalDateTime getOpenedAt() {
            return openedAt;
        }
    }
}
