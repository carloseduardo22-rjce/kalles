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
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.service.PermissionService;
import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.entity.SaleAuditEvent;
import dev.kalles.sale.repository.SaleAuditEventRepository;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.service.CheckoutSessionService;
import dev.kalles.shared.service.Session;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleCancellationService - Cancelamento de Venda")
class SaleCancellationServiceTest {

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

    @InjectMocks
    private SaleCancellationService saleCancellationService;

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
    @DisplayName("Cancelamento de Venda - US005")
    class CancelamentoVenda {

        @Test
        @DisplayName("Cenário 1 - Supervisor pode cancelar venda sem autorização")
        void supervisorPodeCancelarVendaSemAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canCancelSale(supervisorOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleCancellationService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            assertEquals("CANCELED", sale.getStateName());
            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Cenário 2 - Operador básico não pode cancelar sem autorização")
        void operadorBasicoNaoPodeCancelarSemAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(permissionService.canCancelSale(basicOperator)).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () -> 
                saleCancellationService.cancelSale(SESSION_TOKEN, basicOperator.getId())
            );

            assertTrue(exception.getMessage().contains("não possui permissão para cancelar vendas"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cenário 3 - Operador básico pode cancelar com autorização de supervisor")
        void operadorBasicoPodeCancelarComAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeCancellation(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleCancellationService.cancelSaleWithAuthorization(
                SESSION_TOKEN, basicOperator.getId(), supervisorOperator.getId());

            assertEquals("CANCELED", sale.getStateName());
            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Cenário 4 - Deve registrar auditoria do cancelamento sem autorização")
        void deveRegistrarAuditoriaCancelamentoSemAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canCancelSale(supervisorOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleCancellationService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Cenário 4 - Deve registrar auditoria do cancelamento com autorização")
        void deveRegistrarAuditoriaCancelamentoComAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeCancellation(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleCancellationService.cancelSaleWithAuthorization(
                SESSION_TOKEN, basicOperator.getId(), supervisorOperator.getId());

            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Deve impedir cancelamento quando autorizador não tem nível suficiente")
        void deveImpedirCancelamentoQuandoAutorizadorNaoTemNivel() {
            Operator outroBasic = new Operator();
            outroBasic.setId(UUID.randomUUID());
            outroBasic.setName("Outro Operador Básico");
            outroBasic.setCompanyId(COMPANY_ID);
            outroBasic.setPermissionLevel(PermissionLevel.BASIC);

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(outroBasic.getId(), COMPANY_ID)).thenReturn(Optional.of(outroBasic));
            when(permissionService.canAuthorizeCancellation(outroBasic, basicOperator)).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () -> 
                saleCancellationService.cancelSaleWithAuthorization(
                    SESSION_TOKEN, basicOperator.getId(), outroBasic.getId())
            );

            assertTrue(exception.getMessage().contains("nível de permissão suficiente"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve cancelar venda em PAYMENT_IN_PROGRESS (cartão recusado / cliente desistiu)")
        void deveCancelarVendaComPagamentoEmAndamento() {
            sale.startPayment();
            assertEquals("PAYMENT_IN_PROGRESS", sale.getStateName());

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canCancelSale(supervisorOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleCancellationService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            assertEquals("CANCELED", sale.getStateName());
            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Deve cancelar venda PAID ainda não concluída")
        void deveCancelarVendaPagaNaoConcluida() {
            sale.startPayment();
            sale.finishPayment();
            assertEquals("PAID", sale.getStateName());

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canCancelSale(supervisorOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionId(SESSION_ID)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleCancellationService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            assertEquals("CANCELED", sale.getStateName());
            verify(auditRepository).save(any(SaleAuditEvent.class));
        }
    }
}
