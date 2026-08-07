package dev.kalles.core.service;

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
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.core.entity.Payment;
import dev.kalles.core.entity.Product;
import dev.kalles.core.entity.Sale;
import dev.kalles.core.enums.operator.PermissionLevel;
import dev.kalles.core.enums.payment.PaymentMethod;
import dev.kalles.core.exception.ForbiddenOperationException;
import dev.kalles.core.entity.SaleAuditEvent;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.core.repository.SaleAuditEventRepository;
import dev.kalles.core.repository.SaleRepository;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.inventory.entity.Stock;
import dev.kalles.inventory.repository.StockRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService - Serviço de Venda")
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CheckoutSessionService checkoutSessionService;
    
    @Mock
    private OperatorRepository operatorRepository;
    
    @Mock
    private PermissionService permissionService;

    @Mock
    private SaleAuditEventRepository auditRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private FidelityService fidelityService;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private SaleService saleService;

    private static final String SESSION_TOKEN = "session-123";
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



        sale = Sale.createForSession(SESSION_TOKEN);
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
        lenient().when(session.isOpen()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Nested
    @DisplayName("Criação de venda por sessão")
    class CriacaoVendaPorSessao {

        @Test
        @DisplayName("Deve retornar venda ativa existente sem criar nova")
        void deveRetornarVendaExistente() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            Sale result = saleService.getOrCreateSale(SESSION_TOKEN);

            assertSame(sale, result);
            verify(saleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve traduzir violação do índice único (corrida) em erro de negócio")
        void deveTraduzirCorridaDeCriacaoEmErroDeNegocio() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.empty());
            when(saleRepository.saveAndFlush(any(Sale.class)))
                    .thenThrow(new org.springframework.dao.DataIntegrityViolationException("uk_sale_active_per_session"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> saleService.getOrCreateSale(SESSION_TOKEN));

            assertTrue(exception.getMessage().contains("Já existe uma venda ativa"));
        }
    }

    @Nested
    @DisplayName("Cenário 1 - Remoção por operador autorizado")
    class RemocaoComPermissao {

        @Test
        @DisplayName("Deve remover item por código interno quando operador tem permissão")
        void deveRemoverItemPorCodigoInternoComPermissao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCodeAndTenantId(INTERNAL_CODE, TENANT_ID)).thenReturn(Optional.of(product));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            assertDoesNotThrow(() -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, INTERNAL_CODE, supervisorOperator.getId())
            );

            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Deve remover item por código de barras quando operador tem permissão")
        void deveRemoverItemPorCodigoDeBarrasComPermissao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByBarcodeAndTenantId(BAR_CODE, TENANT_ID)).thenReturn(Optional.of(product));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            assertDoesNotThrow(() -> 
                saleService.removeItemByBarCode(SESSION_TOKEN, BAR_CODE, supervisorOperator.getId())
            );

            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Deve recalcular totais após remoção do item")
        void deveRecalcularTotaisAposRemocao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCodeAndTenantId(INTERNAL_CODE, TENANT_ID)).thenReturn(Optional.of(product));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.removeItemByInternalCode(SESSION_TOKEN, INTERNAL_CODE, supervisorOperator.getId());

            assertEquals(BigDecimal.ZERO, sale.getTotal());
            assertEquals(BigDecimal.ZERO, sale.getSubtotal());
        }
    }

    @Nested
    @DisplayName("Cenário 2 - Operador sem permissão é bloqueado")
    class RemocaoSemPermissao {

        @Test
        @DisplayName("Deve impedir remoção por código interno quando operador não tem permissão")
        void deveImpedirRemocaoPorCodigoInternoSemPermissao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(permissionService.canRemoveItens(basicOperator)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, INTERNAL_CODE, basicOperator.getId())
            );

            assertTrue(exception.getMessage().contains("permissão"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve impedir remoção por código de barras quando operador não tem permissão")
        void deveImpedirRemocaoPorCodigoDeBarrasSemPermissao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(permissionService.canRemoveItens(basicOperator)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByBarCode(SESSION_TOKEN, BAR_CODE, basicOperator.getId())
            );

            assertTrue(exception.getMessage().contains("permissão"));
            verify(saleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenário 3 - Remoção com autorização de supervisor")
    class RemocaoComAutorizacao {

        @Test
        @DisplayName("Deve remover item por código interno com autorização válida")
        void deveRemoverPorCodigoInternoComAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeRemoval(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCodeAndTenantId(INTERNAL_CODE, TENANT_ID)).thenReturn(Optional.of(product));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            assertDoesNotThrow(() -> 
                saleService.removeItemByInternalCodeWithAuthorization(
                    SESSION_TOKEN, INTERNAL_CODE, basicOperator.getId(), supervisorOperator.getId())
            );

            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Deve remover item por código de barras com autorização válida")
        void deveRemoverPorCodigoDeBarrasComAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeRemoval(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByBarcodeAndTenantId(BAR_CODE, TENANT_ID)).thenReturn(Optional.of(product));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            assertDoesNotThrow(() -> 
                saleService.removeItemByBarCodeWithAuthorization(
                    SESSION_TOKEN, BAR_CODE, basicOperator.getId(), supervisorOperator.getId())
            );

            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Deve impedir remoção quando autorizador não tem nível suficiente")
        void deveImpedirQuandoAutorizadorNaoTemNivel() {
            Operator outroBasic = new Operator();
            outroBasic.setId(UUID.randomUUID());
            outroBasic.setName("Outro Operador Básico");
            outroBasic.setCompanyId(COMPANY_ID);
            outroBasic.setPermissionLevel(PermissionLevel.BASIC);

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(outroBasic.getId(), COMPANY_ID)).thenReturn(Optional.of(outroBasic));
            when(permissionService.canAuthorizeRemoval(outroBasic, basicOperator)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCodeWithAuthorization(
                    SESSION_TOKEN, INTERNAL_CODE, basicOperator.getId(), outroBasic.getId())
            );

            assertTrue(exception.getMessage().contains("nível de permissão"));
            verify(saleRepository, never()).save(any());
        }
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
            when(saleRepository.findCancellableSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

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
                saleService.cancelSale(SESSION_TOKEN, basicOperator.getId())
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
            when(saleRepository.findCancellableSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSaleWithAuthorization(
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
            when(saleRepository.findCancellableSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Cenário 4 - Deve registrar auditoria do cancelamento com autorização")
        void deveRegistrarAuditoriaCancelamentoComAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(basicOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeCancellation(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findCancellableSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSaleWithAuthorization(
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
                saleService.cancelSaleWithAuthorization(
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
            when(saleRepository.findCancellableSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

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
            when(saleRepository.findCancellableSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            assertEquals("CANCELED", sale.getStateName());
            verify(auditRepository).save(any(SaleAuditEvent.class));
        }
    }

    @Nested
    @DisplayName("BR001 - Validações de sessão e venda")
    class ValidacoesSessaoVenda {

        @Test
        @DisplayName("Deve lançar exceção quando sessão não existe")
        void deveLancarExcecaoQuandoSessaoNaoExiste() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN))
                .thenThrow(new RuntimeException("Sessão de caixa não encontrada"));

            assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, INTERNAL_CODE, supervisorOperator.getId())
            );

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há venda em andamento")
        void deveLancarExcecaoQuandoNaoHaVendaEmAndamento() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, INTERNAL_CODE, supervisorOperator.getId())
            );

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não é encontrado por código interno")
        void deveLancarExcecaoQuandoProdutoNaoEncontradoPorCodigoInterno() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(supervisorOperator.getId(), COMPANY_ID)).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCodeAndTenantId("INVALIDO", TENANT_ID)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, "INVALIDO", supervisorOperator.getId())
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando operador não é encontrado")
        void deveLancarExcecaoQuandoOperadorNaoEncontrado() {
            UUID operadorInexistente = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findByIdAndCompanyId(operadorInexistente, COMPANY_ID)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, INTERNAL_CODE, operadorInexistente)
            );

            verify(saleRepository, never()).save(any());
        }
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
            when(saleRepository.findPaidSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
                when(stockRepository.sumQuantityByProductId(product.getId(), COMPANY_ID)).thenReturn(10);
                when(stockRepository.findAllByProductIdOrderByQuantityDesc(product.getId(), COMPANY_ID))
                    .thenReturn(java.util.List.of(new Stock(UUID.randomUUID(), null, product, null, 10)));

            saleService.completeSale(SESSION_TOKEN);

            assertEquals("COMPLETED", sale.getStateName());
            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Cenário 2 — Deve bloquear finalização quando não há venda paga")
        void deveBloquearFinalizacaoQuandoNaoHaVendaPaga() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findPaidSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> saleService.completeSale(SESSION_TOKEN));
            verify(saleRepository, never()).save(any());
        }
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
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.applyItemDiscount(SESSION_TOKEN, itemId, new BigDecimal("5.00"), supervisorOperator.getId(), null);

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
                saleService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE, basicOperator.getId(), null)
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
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.applyItemDiscount(SESSION_TOKEN, itemId, new BigDecimal("5.00"), basicOperator.getId(), supervisorOperator.getId());

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
                saleService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE, supervisorOperator.getId(), null)
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
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () ->
                saleService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE, supervisorOperator.getId(), null)
            );

            verify(saleRepository, never()).save(any());
        }
    }
}
