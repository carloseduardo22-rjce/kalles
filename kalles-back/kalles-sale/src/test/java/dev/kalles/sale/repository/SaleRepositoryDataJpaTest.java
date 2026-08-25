package dev.kalles.sale.repository;

import dev.kalles.product.entity.Product;
import dev.kalles.sale.dto.SessionPaymentMethodTotal;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SaleRepository devolve uma venda por linha mesmo buscando duas colecoes de uma vez")
class SaleRepositoryDataJpaTest extends AbstractDataJpaTest {

    private static final String SESSION_TOKEN = "8f14e45f-ceea-467a-9d1e-1c1d3f0a1b2c";
    private static final UUID SESSION_ID = UUID.fromString(SESSION_TOKEN);
    private static final UUID TENANT_ID = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000002");
    private static final UUID CASH_REGISTER_ID = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000003");
    private static final UUID OPERATOR_ID = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000004");
    private static final LocalDateTime SESSION_OPENED_AT = LocalDateTime.of(2026, 3, 10, 8, 0);

    @Autowired
    private SaleRepository saleRepository;

    @BeforeEach
    void seedCompany() {
        seedTenantAndCompany(TENANT_ID, COMPANY_ID);
        seedCashRegister(CASH_REGISTER_ID, COMPANY_ID, "CX-REPO");
        seedOperator(OPERATOR_ID, COMPANY_ID, "OP-REPO");
        seedClosedSession(SESSION_ID, CASH_REGISTER_ID, OPERATOR_ID, SESSION_OPENED_AT);
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

        List<Sale> completedSales = saleRepository.findAllBySessionTokenAndStateIn(SESSION_TOKEN, List.of(new CompletedState()));

        assertThat(completedSales).hasSize(1);
        assertThat(completedSales.getFirst().getId()).isEqualTo(completed.getId());
        assertThat(completedSales.getFirst().getItems()).hasSize(2);
        assertThat(completedSales.getFirst().getPayments()).hasSize(2);
    }

    @Test
    @DisplayName("countCanceledBySessionToken conta vendas, nao linhas de item e pagamento")
    void shouldCountCanceledSalesWithoutMultiplyingByTheCollections() {
        Sale canceled = persistSaleWithTwoItemsAndTwoPayments(new CanceledState());
        persistSaleWithTwoItemsAndTwoPayments(new CanceledState());
        persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        detach();

        assertThat(rowsProducedByJoiningBothCollections(canceled.getId())).isEqualTo(4);

        assertThat(saleRepository.countCanceledBySessionToken(SESSION_TOKEN)).isEqualTo(2);
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

    @Test
    @DisplayName("A projecao devolve os mesmos totais que a soma em memoria das vendas concluidas")
    void shouldProjectTheSameTotalsTheInMemorySumProduces() {
        persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        persistSaleWithMixedPayments(new CompletedState());
        persistSaleWithMixedPayments(new CanceledState());
        detach();

        List<Sale> completedSales = saleRepository.findAllBySessionTokenAndStateIn(SESSION_TOKEN, List.of(new CompletedState()));
        BigDecimal totalInMemory = completedSales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<PaymentMethod, BigDecimal> byMethodInMemory = completedSales.stream()
                .flatMap(sale -> sale.getPayments().stream())
                .filter(Payment::isConfirmed)
                .collect(Collectors.groupingBy(
                        Payment::getMethod,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                payment -> payment.getAmount().subtract(payment.getChangeAmount()),
                                BigDecimal::add)));

        BigDecimal projectedTotal = saleRepository.sumCompletedTotalBySessionToken(SESSION_TOKEN);
        Map<PaymentMethod, BigDecimal> projectedByMethod = saleRepository
                .sumCompletedPaymentsByMethod(SESSION_TOKEN)
                .stream()
                .collect(Collectors.toMap(SessionPaymentMethodTotal::method, SessionPaymentMethodTotal::total));

        assertThat(projectedTotal).isEqualByComparingTo(totalInMemory);
        assertThat(projectedByMethod.keySet()).isEqualTo(byMethodInMemory.keySet());
        assertThat(projectedByMethod).allSatisfy((method, total) ->
                assertThat(total).isEqualByComparingTo(byMethodInMemory.get(method)));
    }

    @Test
    @DisplayName("A projecao desconta o troco, ignora pagamento nao confirmado e nao soma venda cancelada")
    void shouldProjectOnlyConfirmedPaymentsOfCompletedSalesNetOfChange() {
        persistSaleWithMixedPayments(new CompletedState());
        persistSaleWithMixedPayments(new CanceledState());
        detach();

        Map<PaymentMethod, BigDecimal> byMethod = saleRepository
                .sumCompletedPaymentsByMethod(SESSION_TOKEN)
                .stream()
                .collect(Collectors.toMap(SessionPaymentMethodTotal::method, SessionPaymentMethodTotal::total));

        assertThat(byMethod).containsOnlyKeys(PaymentMethod.CASH, PaymentMethod.PIX);
        assertThat(byMethod.get(PaymentMethod.CASH)).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(byMethod.get(PaymentMethod.PIX)).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("A projecao do total devolve zero, e nao nulo, em sessao sem venda concluida")
    void shouldProjectZeroWhenTheSessionHasNoCompletedSale() {
        persistSaleWithTwoItemsAndTwoPayments(new CanceledState());
        detach();

        assertThat(saleRepository.sumCompletedTotalBySessionToken(SESSION_TOKEN))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saleRepository.sumCompletedPaymentsByMethod(SESSION_TOKEN)).isEmpty();
    }

    @Test
    @DisplayName("sumCompletedTotalsBetween alcanca a venda pela data de abertura da sessao quando ela nao tem timestamp proprio")
    void shouldSumCompletedTotalsFallingBackToTheSessionOpeningDate() {
        Sale sale = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        detach();
        clearOwnTimestamps(sale.getId());

        BigDecimal total = saleRepository.sumCompletedTotalsBetween(
                COMPANY_ID, SESSION_OPENED_AT.minusDays(1), SESSION_OPENED_AT.plusDays(1));

        assertThat(total).isEqualByComparingTo(sale.getTotal());
    }

    @Test
    @DisplayName("sumCompletedTotalsBetween deixa de fora a venda cuja sessao abriu fora da janela")
    void shouldIgnoreSalesWhoseSessionOpenedOutsideTheWindow() {
        Sale sale = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        detach();
        clearOwnTimestamps(sale.getId());

        BigDecimal total = saleRepository.sumCompletedTotalsBetween(
                COMPANY_ID, SESSION_OPENED_AT.plusDays(1), SESSION_OPENED_AT.plusDays(2));

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("findHistoryRows so devolve a venda quando a sessao leva a um caixa da mesma filial")
    void shouldListHistoryRowsJoinedToTheSessionCashRegister() {
        Sale sale = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        detach();
        clearOwnTimestamps(sale.getId());

        List<SaleRepository.SaleHistoryRow> rows = saleRepository.findHistoryRows(
                COMPANY_ID, SESSION_OPENED_AT.minusDays(1), SESSION_OPENED_AT.plusDays(1));

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getId()).isEqualTo(sale.getId().toString());
        assertThat(rows.getFirst().getOpenedAt()).isEqualTo(SESSION_OPENED_AT);
    }

    @Test
    @DisplayName("findHistoryRowsByState filtra pelo estado sem perder o join com a sessao")
    void shouldFilterHistoryRowsByState() {
        Sale completed = persistSaleWithTwoItemsAndTwoPayments(new CompletedState());
        Sale canceled = persistSaleWithTwoItemsAndTwoPayments(new CanceledState());
        detach();
        clearOwnTimestamps(completed.getId());
        clearOwnTimestamps(canceled.getId());

        List<SaleRepository.SaleHistoryRow> rows = saleRepository.findHistoryRowsByState(
                COMPANY_ID, SESSION_OPENED_AT.minusDays(1), SESSION_OPENED_AT.plusDays(1), CompletedState.NAME);

        assertThat(rows).extracting(SaleRepository.SaleHistoryRow::getId)
                .containsExactly(completed.getId().toString());
    }

    private void clearOwnTimestamps(UUID saleId) {
        entityManager().getEntityManager()
                .createNativeQuery("UPDATE sale SET created_at = NULL, completed_at = NULL WHERE id = :saleId")
                .setParameter("saleId", saleId)
                .executeUpdate();
    }

    private Sale persistSaleWithMixedPayments(SaleState state) {
        Product product = persistProduct("Produto C");

        Sale sale = new Sale();
        sale.setSessionToken(SESSION_TOKEN);
        sale.setCompanyId(COMPANY_ID);
        sale.setState(state);
        sale.setSubtotal(new BigDecimal("55.00"));
        sale.setTotal(new BigDecimal("55.00"));
        sale.setAmountDue(BigDecimal.ZERO);

        sale.getItems().add(new SaleItem(sale, product, 5, new BigDecimal("11.00")));
        sale.getPayments().add(new Payment(sale, PaymentMethod.CASH, new BigDecimal("50.00"), new BigDecimal("5.00"), null, true));
        sale.getPayments().add(new Payment(sale, PaymentMethod.PIX, new BigDecimal("10.00"), BigDecimal.ZERO, null, true));
        sale.getPayments().add(new Payment(sale, PaymentMethod.CREDIT_CARD, new BigDecimal("30.00"), BigDecimal.ZERO, null, false));

        return entityManager().persist(sale);
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
