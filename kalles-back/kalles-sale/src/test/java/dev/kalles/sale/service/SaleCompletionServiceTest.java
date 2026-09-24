package dev.kalles.sale.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.inventory.service.StockService;
import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.service.CheckoutSessionService;
import dev.kalles.shared.service.Session;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleCompletionService - Finalização de Venda")
class SaleCompletionServiceTest {

    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private CheckoutSessionService checkoutSessionService;
    
    @Mock
    private StockService stockService;

    @Mock
    private FidelityService fidelityService;

    @InjectMocks
    private SaleCompletionService saleCompletionService;

    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final String SESSION_TOKEN = SESSION_ID.toString();
    private static final String INTERNAL_CODE = "PRD-001";
    private static final String BAR_CODE = "7891234567890";
    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");
    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private Product product;
    private Sale sale;
    private Operator supervisorOperator;
    private Operator basicOperator;
    private Session session;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        TenantContextHolder.setTenantId(TENANT_ID);

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Produto Teste");
        product.setInternalCode(INTERNAL_CODE);
        product.setBarcode(BAR_CODE);
        product.setTenantId(TENANT_ID);



        sale = Sale.createForSession(SESSION_ID);
        sale.setId(UUID.randomUUID());
        sale.addItem(product, new BigDecimal("25.50"));

        supervisorOperator = new Operator();
        supervisorOperator.setId(UUID.randomUUID());
        supervisorOperator.setName("Supervisor");
        supervisorOperator.setCompanyId(COMPANY_ID);
        supervisorOperator.setPermissionLevel(PermissionLevel.SUPERVISOR);

        basicOperator = new Operator();
        basicOperator.setId(UUID.randomUUID());
        basicOperator.setName("Operador Básico");
        basicOperator.setCompanyId(COMPANY_ID);
        basicOperator.setPermissionLevel(PermissionLevel.BASIC);

        session = mock(Session.class);
        lenient().when(session.getId()).thenReturn(SESSION_ID);
        lenient().when(session.isOpen()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Nested
    @DisplayName("US010 - Finalização da Venda")
    class FinalizacaoVenda {

        @Test
        @DisplayName("Cenário 1 — Deve finalizar venda paga com sucesso")
        void deveFinalizarVendaPagaComSucesso() {
            sale.startPayment();
            Payment payment = new Payment(sale, PaymentMethod.CASH,
                    new BigDecimal("25.50"), BigDecimal.ZERO, null, true);
            sale.addPayment(payment);
            assertEquals("PAID", sale.getStateName());
            assertEquals(0, BigDecimal.ZERO.compareTo(sale.getAmountDue()));

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findPaidSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));

            saleCompletionService.completeSale(SESSION_TOKEN);

            assertEquals("COMPLETED", sale.getStateName());
            verify(stockService).deduct(product, 1, COMPANY_ID);
            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Cenário 2 — Deve bloquear finalização quando não há venda paga")
        void deveBloquearFinalizacaoQuandoNaoHaVendaPaga() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findPaidSaleBySessionId(SESSION_ID)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> saleCompletionService.completeSale(SESSION_TOKEN));
            verify(saleRepository, never()).save(any());
        }
    }
}
