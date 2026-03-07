package dev.kalles.sale.core.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.entity.Payment;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.exception.ForbiddenOperationException;
import dev.kalles.sale.core.entity.SaleAuditEvent;
import dev.kalles.sale.core.repository.ClientRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.SaleAuditEventRepository;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.repository.StockRepository;

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
        product.setPrice(BigDecimal.TEN);
        product.setActive(true);

        sale = Sale.createForSession(SESSION_TOKEN);
        sale.setId(UUID.randomUUID());
        sale.addItem(product);

        supervisorOperator = new Operator();
        supervisorOperator.setId(UUID.randomUUID());
        supervisorOperator.setName("Supervisor");
        supervisorOperator.setPermissionLevel(PermissionLevel.SUPERVISOR);

        basicOperator = new Operator();
        basicOperator.setId(UUID.randomUUID());
        basicOperator.setName("Operador Básico");
        basicOperator.setPermissionLevel(PermissionLevel.BASIC);

        session = mock(Session.class);
        lenient().when(session.isOpen()).thenReturn(true);
    }

    @Nested
    @DisplayName("Cenário 1 - Remoção por operador autorizado")
    class RemocaoComPermissao {

        @Test
        @DisplayName("Deve remover item por código interno quando operador tem permissão")
        void deveRemoverItemPorCodigoInternoComPermissao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCode(INTERNAL_CODE)).thenReturn(Optional.of(product));
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
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByBarcode(BAR_CODE)).thenReturn(Optional.of(product));
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
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCode(INTERNAL_CODE)).thenReturn(Optional.of(product));
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
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
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
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
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
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeRemoval(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCode(INTERNAL_CODE)).thenReturn(Optional.of(product));
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
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeRemoval(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByBarcode(BAR_CODE)).thenReturn(Optional.of(product));
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
            outroBasic.setPermissionLevel(PermissionLevel.BASIC);

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findById(outroBasic.getId())).thenReturn(Optional.of(outroBasic));
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
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canCancelSale(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            assertEquals("CANCELED", sale.getStateName());
            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Cenário 2 - Operador básico não pode cancelar sem autorização")
        void operadorBasicoNaoPodeCancelarSemAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
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
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeCancellation(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
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
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canCancelSale(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.cancelSale(SESSION_TOKEN, supervisorOperator.getId());

            verify(auditRepository).save(any(SaleAuditEvent.class));
        }

        @Test
        @DisplayName("Cenário 4 - Deve registrar auditoria do cancelamento com autorização")
        void deveRegistrarAuditoriaCancelamentoComAutorizacao() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canAuthorizeCancellation(supervisorOperator, basicOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
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
            outroBasic.setPermissionLevel(PermissionLevel.BASIC);

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findById(basicOperator.getId())).thenReturn(Optional.of(basicOperator));
            when(operatorRepository.findById(outroBasic.getId())).thenReturn(Optional.of(outroBasic));
            when(permissionService.canAuthorizeCancellation(outroBasic, basicOperator)).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () -> 
                saleService.cancelSaleWithAuthorization(
                    SESSION_TOKEN, basicOperator.getId(), outroBasic.getId())
            );

            assertTrue(exception.getMessage().contains("nível de permissão suficiente"));
            verify(saleRepository, never()).save(any());
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
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
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
            when(operatorRepository.findById(supervisorOperator.getId())).thenReturn(Optional.of(supervisorOperator));
            when(permissionService.canRemoveItens(supervisorOperator)).thenReturn(true);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(productRepository.findByInternalCode("INVALIDO")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> 
                saleService.removeItemByInternalCode(SESSION_TOKEN, "INVALIDO", supervisorOperator.getId())
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando operador não é encontrado")
        void deveLancarExcecaoQuandoOperadorNaoEncontrado() {
            UUID operadorInexistente = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(operatorRepository.findById(operadorInexistente)).thenReturn(Optional.empty());

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
                    BigDecimal.TEN, BigDecimal.ZERO, null, true);
            sale.addPayment(payment);
            assertEquals("PAID", sale.getStateName());
            assertEquals(0, BigDecimal.ZERO.compareTo(sale.getAmountDue()));

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findPaidSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

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
        @DisplayName("Deve aplicar desconto com sucesso via service")
        void deveAplicarDescontoComSucesso() {
            UUID itemId = sale.getItems().stream().findFirst().orElseThrow().getId();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);

            saleService.applyItemDiscount(SESSION_TOKEN, itemId, new BigDecimal("5.00"));

            assertEquals(new BigDecimal("5.00"), sale.getItems().stream().findFirst().orElseThrow().getDiscount());
            assertEquals(new BigDecimal("5.00"), sale.getTotal());
            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("Deve lançar exceção quando sessão não existe")
        void deveLancarExcecaoQuandoSessaoNaoExiste() {
            UUID itemId = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN))
                .thenThrow(new RuntimeException("Sessão de caixa não encontrada"));

            assertThrows(RuntimeException.class, () ->
                saleService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE)
            );

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há venda em andamento")
        void deveLancarExcecaoQuandoNaoHaVendaEmAndamento() {
            UUID itemId = UUID.randomUUID();
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () ->
                saleService.applyItemDiscount(SESSION_TOKEN, itemId, BigDecimal.ONE)
            );

            verify(saleRepository, never()).save(any());
        }
    }
}
