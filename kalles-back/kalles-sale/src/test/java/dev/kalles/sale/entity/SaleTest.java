package dev.kalles.sale.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.core.enums.operator.PermissionLevel;
import dev.kalles.product.entity.Product;

@DisplayName("Sale - Entidade de Domínio da Venda")
class SaleTest {

    private Sale sale;
    private Product product;
    private BigDecimal productPrice;
    private Operator supervisor;
    private Operator basic;

    @BeforeEach
    void setUp() {
        productPrice = new BigDecimal("25.50");
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Produto Teste");
        product.setInternalCode("PRD-001");
        product.setBarcode("7891234567890");
        

        sale = Sale.createForSession("session-123");
        sale.setId(UUID.randomUUID());

        supervisor = new Operator();
        supervisor.setId(UUID.randomUUID());
        supervisor.setName("Supervisor");
        supervisor.setPermissionLevel(PermissionLevel.SUPERVISOR);

        basic = new Operator();
        basic.setId(UUID.randomUUID());
        basic.setName("Operador Básico");
        basic.setPermissionLevel(PermissionLevel.BASIC);
    }

    @Nested
    @DisplayName("Remoção de item da venda")
    class RemocaoDeItem {

        @Test
        @DisplayName("Deve remover item existente da venda")
        void deveRemoverItemExistente() {
            sale.addItem(product, new BigDecimal("25.50"));
            assertEquals(1, sale.getItems().size());

            sale.removeItem(product);

            assertTrue(sale.getItems().isEmpty());
        }

        @Test
        @DisplayName("Não deve lançar erro ao tentar remover item inexistente")
        void naoDeveLancarErroAoRemoverItemInexistente() {
            Product outroProduto = new Product();
            outroProduto.setId(UUID.randomUUID());
            BigDecimal outroProdutoPrice = new BigDecimal("10.00");

            sale.addItem(product, new BigDecimal("25.50"));

            assertDoesNotThrow(() -> sale.removeItem(outroProduto));
            assertEquals(1, sale.getItems().size());
        }
    }

    @Nested
    @DisplayName("Recálculo de totais após remoção")
    class RecalculoTotais {

        @Test
        @DisplayName("Deve zerar total quando único item é removido")
        void deveZerarTotalQuandoUnicoItemRemovido() {
            sale.addItem(product, new BigDecimal("25.50"));
            assertEquals(new BigDecimal("25.50"), sale.getTotal());

            sale.removeItem(product);

            assertEquals(BigDecimal.ZERO, sale.getTotal());
            assertEquals(BigDecimal.ZERO, sale.getSubtotal());
        }

        @Test
        @DisplayName("Deve recalcular total quando um de múltiplos itens é removido")
        void deveRecalcularTotalAoRemoverUmDeMuitos() {
            Product outroProduto = new Product();
            outroProduto.setId(UUID.randomUUID());
            outroProduto.setName("Outro Produto");
            BigDecimal outroProdutoPrice = new BigDecimal("10.00");

            sale.addItem(product, new BigDecimal("25.50"));
            sale.addItem(outroProduto, outroProdutoPrice);
            assertEquals(new BigDecimal("35.50"), sale.getTotal());

            sale.removeItem(product);

            assertEquals(new BigDecimal("10.00"), sale.getTotal());
        }

        @Test
        @DisplayName("Deve adicionar varias unidades do item de uma vez")
        void deveAdicionarVariasUnidadesDeUmaVez() {
            sale.addItem(product, productPrice, 4);

            assertEquals(1, sale.getItems().size());
            assertEquals(4, sale.getItems().iterator().next().getQuantity());
            assertEquals(new BigDecimal("102.00"), sale.getTotal());
        }
    }

    @Nested
    @DisplayName("Remoção bloqueada por estado da venda")
    class BloqueioEstado {

        @Test
        @DisplayName("Deve impedir remoção quando venda está cancelada")
        void deveImpedirRemocaoQuandoVendaCancelada() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.cancel();

            assertThrows(IllegalStateException.class, () -> 
                sale.removeItem(product)
            );
        }

        @Test
        @DisplayName("Deve impedir remoção quando venda está em pagamento")
        void deveImpedirRemocaoQuandoVendaEmPagamento() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();

            assertThrows(IllegalStateException.class, () -> 
                sale.removeItem(product)
            );
        }

        @Test
        @DisplayName("Deve impedir remoção quando venda está paga")
        void deveImpedirRemocaoQuandoVendaPaga() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();

            assertThrows(IllegalStateException.class, () -> 
                sale.removeItem(product)
            );
        }
    }

    @Nested
    @DisplayName("US010 - Finalização da Venda")
    class FinalizacaoVenda {

        @Test
        @DisplayName("Cenário 1 — Deve finalizar venda paga com sucesso")
        void deveFinalizarVendaPagaComSucesso() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();
            assertEquals("PAID", sale.getStateName());

            sale.completeSale();

            assertEquals("COMPLETED", sale.getStateName());
        }

        @Test
        @DisplayName("Cenário 2 — Deve impedir finalização quando venda está aberta")
        void deveImpedirFinalizacaoQuandoVendaAberta() {
            sale.addItem(product, new BigDecimal("25.50"));

            assertThrows(IllegalStateException.class, () -> sale.completeSale());
        }

        @Test
        @DisplayName("Deve impedir finalização quando venda está em pagamento")
        void deveImpedirFinalizacaoQuandoVendaEmPagamento() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();

            assertThrows(IllegalStateException.class, () -> sale.completeSale());
        }

        @Test
        @DisplayName("Deve impedir finalização quando venda está cancelada")
        void deveImpedirFinalizacaoQuandoVendaCancelada() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.cancel();

            assertThrows(IllegalStateException.class, () -> sale.completeSale());
        }

        @Test
        @DisplayName("BR010 — Venda concluída não aceita novos itens")
        void vendaConcluidaNaoAceitaNovosItens() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();
            sale.completeSale();

            assertThrows(IllegalStateException.class, () -> sale.addItem(product, new BigDecimal("25.50")));
        }

        @Test
        @DisplayName("BR010 — Venda concluída não aceita novos pagamentos")
        void vendaConcluidaNaoAceitaNovoPagamento() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();
            sale.completeSale();

            assertThrows(IllegalStateException.class, () -> sale.startPayment());
        }

        @Test
        @DisplayName("BR010 — Venda concluída não pode ser cancelada")
        void vendaConcluidaNaoPodeSerCancelada() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();
            sale.completeSale();

            assertThrows(IllegalStateException.class, () -> sale.cancel());
        }
    }

    @Nested
    @DisplayName("US012 - Desconto no Item")
    class DescontoNoItem {

        @Test
        @DisplayName("Cenário 1 — Deve aplicar desconto por valor fixo e atualizar subtotal")
        void deveAplicarDescontoPorValorFixo() {
            Product produtoCaro = new Product();
            produtoCaro.setId(UUID.randomUUID());
            produtoCaro.setName("Produto Caro");
            BigDecimal produtoCaroPrice = new BigDecimal("100.00");

            sale.addItem(produtoCaro, produtoCaroPrice);
            UUID itemId = sale.getItems().iterator().next().getId();

            sale.applyItemDiscount(itemId, new BigDecimal("10.00"));

            SaleItem item = sale.getItems().iterator().next();
            assertEquals(new BigDecimal("10.00"), item.getDiscount());
            assertEquals(new BigDecimal("90.00"), item.getSubtotal());
            assertEquals(new BigDecimal("90.00"), sale.getSubtotal());
            assertEquals(new BigDecimal("90.00"), sale.getTotal());
        }

        @Test
        @DisplayName("Cenário 2 — Deve bloquear desconto maior que o valor do item")
        void deveBloquearDescontoMaiorQueValorDoItem() {
            Product produtoCaro = new Product();
            produtoCaro.setId(UUID.randomUUID());
            produtoCaro.setName("Produto Caro");
            BigDecimal produtoCaroPrice = new BigDecimal("100.00");

            sale.addItem(produtoCaro, produtoCaroPrice);
            UUID itemId = sale.getItems().iterator().next().getId();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                sale.applyItemDiscount(itemId, new BigDecimal("160.00"))
            );

            assertTrue(exception.getMessage().contains("não pode exceder"));
        }

        @Test
        @DisplayName("BR013 — Deve bloquear desconto negativo")
        void deveBloquearDescontoNegativo() {
            sale.addItem(product, new BigDecimal("25.50"));
            UUID itemId = sale.getItems().iterator().next().getId();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                sale.applyItemDiscount(itemId, new BigDecimal("-5.00"))
            );

            assertTrue(exception.getMessage().contains("não pode ser negativo"));
        }

        @Test
        @DisplayName("Deve recalcular totais da venda após desconto")
        void deveRecalcularTotaisAposDesconto() {
            Product outroProduto = new Product();
            outroProduto.setId(UUID.randomUUID());
            outroProduto.setName("Outro Produto");
            BigDecimal outroProdutoPrice = new BigDecimal("10.00");

sale.addItem(product, new java.math.BigDecimal("45.50"));
            sale.addItem(outroProduto, outroProdutoPrice);
            assertEquals(new java.math.BigDecimal("55.50"), sale.getTotal());

            UUID itemId = sale.getItems().iterator().next().getId();
            sale.applyItemDiscount(itemId, new BigDecimal("5.50"));

            assertEquals(new BigDecimal("50.00"), sale.getTotal());
            assertEquals(new BigDecimal("50.00"), sale.getSubtotal());
        }

        @Test
        @DisplayName("Deve impedir desconto quando venda está cancelada")
        void deveImpedirDescontoQuandoVendaCancelada() {
            sale.addItem(product, new BigDecimal("25.50"));
            UUID itemId = sale.getItems().iterator().next().getId();
            sale.cancel();

            assertThrows(IllegalStateException.class, () ->
                sale.applyItemDiscount(itemId, new BigDecimal("5.00"))
            );
        }

        @Test
        @DisplayName("Deve impedir desconto quando venda está em pagamento")
        void deveImpedirDescontoQuandoVendaEmPagamento() {
            sale.addItem(product, new BigDecimal("25.50"));
            UUID itemId = sale.getItems().iterator().next().getId();
            sale.startPayment();

            assertThrows(IllegalStateException.class, () ->
                sale.applyItemDiscount(itemId, new BigDecimal("5.00"))
            );
        }

        @Test
        @DisplayName("Deve impedir desconto quando venda está paga")
        void deveImpedirDescontoQuandoVendaPaga() {
            sale.addItem(product, new BigDecimal("25.50"));
            UUID itemId = sale.getItems().iterator().next().getId();
            sale.startPayment();
            sale.finishPayment();

            assertThrows(IllegalStateException.class, () ->
                sale.applyItemDiscount(itemId, new BigDecimal("5.00"))
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando item não encontrado na venda")
        void deveLancarExcecaoQuandoItemNaoEncontrado() {
            sale.addItem(product, new BigDecimal("25.50"));

            UUID itemIdInexistente = UUID.randomUUID();

            assertThrows(IllegalArgumentException.class, () ->
                sale.applyItemDiscount(itemIdInexistente, new BigDecimal("5.00"))
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando venda está completa")
        void deveImpedirDescontoQuandoVendaCompleta() {
            sale.addItem(product, new BigDecimal("25.50"));
            UUID itemId = sale.getItems().iterator().next().getId();
            sale.startPayment();
            sale.finishPayment();
            sale.completeSale();

            assertThrows(IllegalStateException.class, () ->
                sale.applyItemDiscount(itemId, new BigDecimal("5.00"))
            );
        }

    }

    @Nested
    @DisplayName("Desconto de Fidelidade na Venda")
    class DescontoFidelidade {

        @Test
        @DisplayName("Deve aplicar desconto de fidelidade quando venda está aberta")
        void deveAplicarDescontoDeFelidadeQuandoAberta() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.applyFidelityDiscount(new BigDecimal("10.00"));

            assertEquals(new BigDecimal("15.50"), sale.getTotal());
            assertEquals(new BigDecimal("25.50"), sale.getSubtotal());
            assertEquals(new BigDecimal("10.00"), sale.getFidelityDiscountApplied());
        }

        @Test
        @DisplayName("Deve bloquear desconto de fidelidade quando venda está cancelada")
        void deveBloquearDescontoFidelidadeQuandoCancelada() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.cancel();

            assertThrows(IllegalStateException.class, () ->
                sale.applyFidelityDiscount(new BigDecimal("10.00"))
            );
        }

        @Test
        @DisplayName("Deve bloquear desconto de fidelidade quando venda está em pagamento")
        void deveBloquearDescontoFidelidadeQuandoEmPagamento() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();

            assertThrows(IllegalStateException.class, () ->
                sale.applyFidelityDiscount(new BigDecimal("10.00"))
            );
        }

        @Test
        @DisplayName("Deve bloquear desconto de fidelidade quando venda está paga")
        void deveBloquearDescontoFidelidadeQuandoPaga() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();

            assertThrows(IllegalStateException.class, () ->
                sale.applyFidelityDiscount(new BigDecimal("10.00"))
            );
        }

        @Test
        @DisplayName("Deve bloquear desconto de fidelidade quando venda está concluída")
        void deveBloquearDescontoFidelidadeQuandoConcluida() {
            sale.addItem(product, new BigDecimal("25.50"));
            sale.startPayment();
            sale.finishPayment();
            sale.completeSale();

            assertThrows(IllegalStateException.class, () ->
                sale.applyFidelityDiscount(new BigDecimal("10.00"))
            );
        }
    }
}
