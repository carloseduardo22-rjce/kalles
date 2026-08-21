package dev.kalles.fidelity.entity;

import dev.kalles.client.entity.Client;
import dev.kalles.fidelity.enums.FidelityDiscountType;
import dev.kalles.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Datas de auditoria das entidades de fidelidade")
class FidelityAuditingDataJpaTest extends AbstractDataJpaTest {

    private static final UUID TENANT_ID = UUID.fromString("f1f2f3f4-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("f1f2f3f4-0000-4000-8000-000000000002");
    private static final LocalDateTime BACKDATED = LocalDateTime.of(2020, 1, 1, 0, 0);

    @BeforeEach
    void seed() {
        seedTenantAndCompany(TENANT_ID, COMPANY_ID);
    }

    @Test
    @DisplayName("a politica gravada tem createdAt")
    void shouldFillTheCreatedAtOfThePersistedPolicy() {
        FidelityPolicy policy = persistPolicy();
        detach();

        FidelityPolicy reloaded = entityManager().find(FidelityPolicy.class, policy.getId());

        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("a fidelidade gravada tem createdAt")
    void shouldFillTheCreatedAtOfThePersistedFidelity() {
        Fidelity fidelity = persistFidelity(persistPolicy());
        detach();

        Fidelity reloaded = entityManager().find(Fidelity.class, fidelity.getId());

        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("o createdAt marca a gravacao, nao o valor trazido pelo objeto")
    void shouldOverrideTheCreatedAtCarriedByTheObject() {
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(COMPANY_ID);
        policy.setObjectivePoints(100);
        policy.setConfiguredDiscount(new BigDecimal("20.00"));
        policy.setValuePoint(1);
        policy.setDiscountType(FidelityDiscountType.FIXED);
        policy.setCreatedAt(BACKDATED);
        entityManager().persist(policy);
        detach();

        FidelityPolicy reloaded = entityManager().find(FidelityPolicy.class, policy.getId());

        assertThat(reloaded.getCreatedAt()).isAfter(BACKDATED);
    }

    private FidelityPolicy persistPolicy() {
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(COMPANY_ID);
        policy.setObjectivePoints(100);
        policy.setConfiguredDiscount(new BigDecimal("20.00"));
        policy.setValuePoint(1);
        policy.setDiscountType(FidelityDiscountType.FIXED);
        entityManager().persist(policy);
        return policy;
    }

    private Fidelity persistFidelity(FidelityPolicy policy) {
        Client client = new Client();
        client.setCompanyId(COMPANY_ID);
        client.setName("Cliente da auditoria");
        entityManager().persist(client);

        Fidelity fidelity = new Fidelity();
        fidelity.setClient(client);
        fidelity.setPolicy(policy);
        fidelity.setPoints(0);
        fidelity.setAvailableDiscount(BigDecimal.ZERO);
        entityManager().persist(fidelity);
        return fidelity;
    }
}
