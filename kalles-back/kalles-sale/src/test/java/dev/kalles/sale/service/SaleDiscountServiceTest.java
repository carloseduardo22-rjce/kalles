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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.service.PermissionService;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.entity.SaleAuditEvent;
import dev.kalles.sale.repository.SaleAuditEventRepository;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.service.CheckoutSessionService;
import dev.kalles.shared.service.Session;
import dev.kalles.testsupport.RequestContextExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleDiscountService - Descontos da Venda")
class SaleDiscountServiceTest {

    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private CheckoutSessionService checkoutSessionService;
    
    @Mock
    private OperatorRepository operatorRepository;
    
    @Mock
    private PermissionService permissionService;

    @Mock
    private SaleAuditEventRepository auditRepository;

    @Mock
    private FidelityService fidelityService;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private SaleDiscountService saleDiscountService;

    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final String SESSION_TOKEN = SESSION_ID.toString();
    private static final String INTERNAL_CODE = "PRD-001";
    private static final String BAR_CODE = "7891234567890";
    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");
    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.tenantAndCompany(TENANT_ID, COMPANY_ID);

    private Product product;
    private Sale sale;
    private Operator supervisorOperator;
    private Operator basicOperator;
    private Session session;

    @BeforeEach
    void setUp() {
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

    @Nested
    @DisplayName("US012 - Desconto no Item via Service")
    class DescontoNoItem {

        @Test
        @DisplayName("Supervisor deve aplicar desconto com sucesso e registrar auditoria")
        void deveAplicarDescontoComSucesso() {
            UUID itemId = sale.getItems().stream().findFirst().orElseThrow().getId();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canApplyItemDiscount(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleDiscountService.applyItemDiscount(SESSION_TOKEN, itemId, new BigDecimal("5.00"), supervisorOperator.getId(), null);

            assertEquals(new BigDecimal("5.00"), sale.getItems().stream().findFirst().orElseThrow().getDiscount());
            assertEquals(new BigDecimal("20.50"), sale.getTotal());
            verify(saleRepository).save(sale);
            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Operador básico não pode aplicar desconto sem autorização")
        void operadorBasicoNaoPodeAplicarDescontoSemAutorizacao() {
            UUID itemId = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(permissionService.canApplyItemDiscount(basicOperator)).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () ->
                saleDiscountService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE, basicOperator.getId(), null)
            );

            assertTrue(exception.getMessage().contains("não possui permissão para aplicar descontos"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Operador básico pode aplicar desconto com autorização de supervisor")
        void operadorBasicoPodeAplicarDescontoComAutorizacao() {
            UUID itemId = sale.getItems().stream().findFirst().orElseThrow().getId();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeItemDiscount(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleDiscountService.applyItemDiscount(SESSION_TOKEN, itemId, new BigDecimal("5.00"), basicOperator.getId(), supervisorOperator.getId());

            assertEquals(new BigDecimal("5.00"), sale.getItems().stream().findFirst().orElseThrow().getDiscount());
            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando sessão não existe")
        void deveLancarExcecaoQuandoSessaoNaoExiste() {
            UUID itemId = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN))
                .thenThrow(new RuntimeException("Sessão de caixa não encontrada"));

            assertThrows(RuntimeException.class, () ->
                saleDiscountService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE, supervisorOperator.getId(), null)
            );

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há venda em andamento")
        void deveLancarExcecaoQuandoNaoHaVendaEmAndamento() {
            UUID itemId = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canApplyItemDiscount(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionId(SESSION_ID)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () ->
                saleDiscountService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE, supervisorOperator.getId(), null)
            );

            verify(saleRepository, never()).save(any());
        }
    }
}
