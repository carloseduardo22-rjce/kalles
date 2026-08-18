package dev.kalles.sale.repository;

import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.entity.SaleItem;
import dev.kalles.sale.state.CanceledState;
import dev.kalles.sale.state.CompletedState;
import dev.kalles.sale.state.OpenState;
import dev.kalles.sale.state.SaleState;
import dev.kalles.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SaleRepository devolve uma venda por linha mesmo buscando duas colecoes de uma vez")
class SaleRepositoryDataJpaTest extends AbstractDataJpaTest {

    private static final String SESSION_TOKEN = "8f14e45f-ceea-467a-9d1e-1c1d3f0a1b2c";
    private static final UUID TENANT_ID = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000002");

    @Autowired
    private SaleRepository saleRepository;

    @BeforeEach
    void seedCompany() {
        seedTenantAndCompany(TENANT_ID, COMPANY_ID);
    }

    @Test
    @DisplayName("findAllByIdIn nao multiplica a venda pelo produto das colecoes")
    void shouldReturnOneSaleWhenFetchingItemsAndPaymentsTogether() {
        Sale sale = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        detach();

        assertThat(rowsProducedByJoiningBothCollections(sale.getId())).isEqualTo(4);

        List<Sale> sales = saleRepository.findAllByIdIn(List.of(sale.getId()));

        assertThat(sales).hasSize(1);
        assertThat(sales.getFirst().getItems()).hasSize(2);
        assertThat(sales.getFirst().getPayments()).hasSize(2);
    }

    private long rowsProducedByJoiningBothCollections(UUID saleId) {
        return ((Number) entityManager().getEntityManager()
                .createNativeQuery("""
                        SELECT COUNT(*) FROM sale s
                        JOIN sale_item i ON i.sale_id = s.id
                        JOIN payment p ON p.sale_id = s.id
                        WHERE s.id = :saleId
                        """)
                .setParameter("saleId", saleId)
                .getSingleResult()).longValue();
    }

    @Test
    @DisplayName("findBySessionTokenAndStateIn devolve Optional sem estourar por resultado duplicado")
    void shouldReturnSingleOptionalWhenSaleHasTwoItemsAndTwoPayments() {
        Sale sale = persistSaleWithTwoItemsAndTwoPayments(new OpenState());
        detach();

        Optional<Sale> found = saleRepository.findActiveSaleBySessionToken(SESSION_TOKEN);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(sale.getId());
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getPayments()).hasSize(2);
    }

    @Test
    @DisplayName("findAllBySessionTokenAndStateIn filtra por estado sem duplicar as vendas que passam")
    void shouldFilterBySessionAndStateWithoutDuplicatingRows() {
        Sale completed = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        persistSaleWithTwoItemsAndTwoPayments(new CanceledState());
        detach();

        List<Sale> completedSales = saleRepository.findCompletedBySessionToken(SESSION_TOKEN);
        List<Sale> canceledSales = saleRepository.findCanceledBySessionToken(SESSION_TOKEN);

        assertThat(completedSales).hasSize(1);
        assertThat(completedSales.getFirst().getId()).isEqualTo(completed.getId());
        assertThat(completedSales.getFirst().getItems()).hasSize(2);
        assertThat(completedSales.getFirst().getPayments()).hasSize(2);
        assertThat(canceledSales).hasSize(1);
    }

    @Test
    @DisplayName("findAllByIdIn devolve uma linha por venda quando o lote tem varias")
    void shouldReturnOneRowPerSaleWhenTheBatchHasSeveral() {
        Sale first = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        Sale second = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        detach();

        List<Sale> sales = saleRepository.findAllByIdIn(List.of(first.getId(), second.getId()));

        assertThat(sales).hasSize(2);
        assertThat(sales).extracting(Sale::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(sales).allSatisfy(sale -> {
            assertThat(sale.getItems()).hasSize(2);
            assertThat(sale.getPayments()).hasSize(2);
        });
    }

    private Sale persistSaleWithTwoItemsAndTwoPayments(SaleState state) {
        Product firstProduct = persistProduct("Produto A");
        Product secondProduct = persistProduct("Produto B");

        Sale sale = new Sale();
        sale.setSessionToken(SESSION_TOKEN);
        sale.setCompanyId(COMPANY_ID);
        sale.setState(state);
        sale.setSubtotal(new BigDecimal("30.00"));
        sale.setTotal(new BigDecimal("30.00"));
        sale.setAmountDue(BigDecimal.ZERO);

        sale.getItems().add(new SaleItem(sale, firstProduct, 1, new BigDecimal("10.00")));
        sale.getItems().add(new SaleItem(sale, secondProduct, 2, new BigDecimal("10.00")));
        sale.getPayments().add(new Payment(sale, PaymentMethod.CASH, new BigDecimal("20.00"), BigDecimal.ZERO, null, true));
        sale.getPayments().add(new Payment(sale, PaymentMethod.PIX, new BigDecimal("10.00"), BigDecimal.ZERO, null, true));

        return entityManager().persist(sale);
    }

    private Product persistProduct(String name) {
        Product product = new Product();
        product.setTenantId(UUID.randomUUID());
        product.setName(name);
        product.setInternalCode(UUID.randomUUID().toString().substring(0, 20));
        return entityManager().persist(product);
    }
}
