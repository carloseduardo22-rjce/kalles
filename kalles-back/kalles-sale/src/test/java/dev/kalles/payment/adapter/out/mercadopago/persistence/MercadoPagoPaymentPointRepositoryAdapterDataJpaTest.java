package dev.kalles.payment.adapter.out.mercadopago.persistence;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoPointEntity;
import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(MercadoPagoPaymentPointRepositoryAdapter.class)
@DisplayName("Adapter de points do Mercado Pago consulta por caixa")
class MercadoPagoPaymentPointRepositoryAdapterDataJpaTest extends AbstractDataJpaTest {

    private static final UUID TENANT_ID = UUID.fromString("b1b2c3d4-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("b1b2c3d4-0000-4000-8000-000000000002");
    private static final UUID CASH_REGISTER_ID = UUID.fromString("b1b2c3d4-0000-4000-8000-000000000003");
    private static final UUID OTHER_CASH_REGISTER_ID = UUID.fromString("b1b2c3d4-0000-4000-8000-000000000004");

    @Autowired
    private MercadoPagoPaymentPointRepositoryAdapter adapter;

    @BeforeEach
    void seedCashRegisters() {
        seedTenantAndCompany(TENANT_ID, COMPANY_ID);
        seedCashRegister(CASH_REGISTER_ID, COMPANY_ID, "CAIXA-REPO-1");
        seedCashRegister(OTHER_CASH_REGISTER_ID, COMPANY_ID, "CAIXA-REPO-2");
    }

    @Test
    @DisplayName("devolve o point do caixa pedido, ignorando os dos outros caixas")
    void shouldFindThePointOfTheRequestedCashRegister() {
        persistPoint("EXT-REPO-1", OTHER_CASH_REGISTER_ID, 11L);
        persistPoint("EXT-REPO-2", CASH_REGISTER_ID, 22L);
        persistPoint("EXT-REPO-3", OTHER_CASH_REGISTER_ID, 33L);
        detach();

        Optional<PaymentPoint> found = adapter.findByCashRegisterIdAndProvider(
                CASH_REGISTER_ID, PaymentProvider.MERCADO_PAGO);

        assertThat(found).isPresent();
        assertThat(found.get().externalReference()).isEqualTo("EXT-REPO-2");
        assertThat(found.get().cashRegisterId()).isEqualTo(CASH_REGISTER_ID);
        assertThat(found.get().providerPointId()).isEqualTo("22");
    }

    @Test
    @DisplayName("devolve vazio quando nenhum point pertence ao caixa")
    void shouldReturnEmptyWhenNoPointBelongsToTheCashRegister() {
        persistPoint("EXT-REPO-4", OTHER_CASH_REGISTER_ID, 44L);
        detach();

        Optional<PaymentPoint> found = adapter.findByCashRegisterIdAndProvider(
                CASH_REGISTER_ID, PaymentProvider.MERCADO_PAGO);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("devolve um point, e nao estoura, quando dois dividem o mesmo caixa")
    void shouldReturnOnePointWhenTwoShareTheSameCashRegister() {
        persistPoint("EXT-REPO-5", CASH_REGISTER_ID, 55L);
        persistPoint("EXT-REPO-6", CASH_REGISTER_ID, 66L);
        detach();

        Optional<PaymentPoint> found = adapter.findByCashRegisterIdAndProvider(
                CASH_REGISTER_ID, PaymentProvider.MERCADO_PAGO);

        assertThat(found).isPresent();
        assertThat(found.get().cashRegisterId()).isEqualTo(CASH_REGISTER_ID);
    }

    private void persistPoint(String externalReference, UUID cashRegisterId, Long providerPointId) {
        MercadoPagoPointEntity entity = new MercadoPagoPointEntity();
        entity.setExternalReference(externalReference);
        entity.setCashRegisterId(cashRegisterId);
        entity.setProviderPointId(providerPointId);
        entityManager().persist(entity);
    }
}
