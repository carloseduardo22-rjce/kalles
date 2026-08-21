package dev.kalles.security.entity;

import dev.kalles.security.enums.AccountRole;
import dev.kalles.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Datas de auditoria das entidades de seguranca")
class SecurityAuditingDataJpaTest extends AbstractDataJpaTest {

    private static final UUID TENANT_ID = UUID.fromString("c1c2c3c4-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("c1c2c3c4-0000-4000-8000-000000000002");

    @BeforeEach
    void seed() {
        seedTenantAndCompany(TENANT_ID, COMPANY_ID);
    }

    @Test
    @DisplayName("a conta gravada tem createdAt")
    void shouldFillTheCreatedAtOfThePersistedAccount() {
        Account account = persistAccount("auditoria.criacao@teste.kalles");
        detach();

        Account reloaded = entityManager().find(Account.class, account.getId());

        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("a conta alterada tem updatedAt")
    void shouldFillTheUpdatedAtOfTheChangedAccount() {
        Account account = persistAccount("auditoria.alteracao@teste.kalles");
        detach();

        Account reloaded = entityManager().find(Account.class, account.getId());
        reloaded.setName("Nome alterado");
        detach();

        assertThat(entityManager().find(Account.class, account.getId()).getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("a sessao de POS gravada tem createdAt")
    void shouldFillTheCreatedAtOfThePersistedPosDeviceSession() {
        PosDeviceSession session = new PosDeviceSession(
                COMPANY_ID, UUID.randomUUID(), "token-de-auditoria", LocalDateTime.now().plusDays(7));
        entityManager().persist(session);
        detach();

        PosDeviceSession reloaded = entityManager().find(PosDeviceSession.class, session.getId());

        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    private Account persistAccount(String email) {
        Account account = new Account(TENANT_ID, "Conta de auditoria", email, "hash", AccountRole.ADMIN);
        entityManager().persist(account);
        return account;
    }
}
