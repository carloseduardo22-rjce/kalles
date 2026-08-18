package dev.kalles.testsupport;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

@Tag("integration")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractDataJpaTest {

    protected static final PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.instance();

    @Autowired
    private TestEntityManager entityManager;

    protected TestEntityManager entityManager() {
        return entityManager;
    }

    protected void seedTenantAndCompany(UUID tenantId, UUID companyId) {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO tenant (id, name) VALUES (:id, 'Tenant do teste de repositorio') ON CONFLICT (id) DO NOTHING")
                .setParameter("id", tenantId)
                .executeUpdate();
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO company (id, name, tenant_id) VALUES (:id, 'Filial do teste de repositorio', :tenantId) ON CONFLICT (id) DO NOTHING")
                .setParameter("id", companyId)
                .setParameter("tenantId", tenantId)
                .executeUpdate();
    }

    protected void seedCashRegister(UUID cashRegisterId, UUID companyId, String code) {
        entityManager.getEntityManager()
                .createNativeQuery("""
                        INSERT INTO cash_registers (id, code, description, active, company_id)
                        VALUES (:id, :code, 'Caixa do teste de repositorio', TRUE, :companyId)
                        ON CONFLICT (id) DO NOTHING
                        """)
                .setParameter("id", cashRegisterId)
                .setParameter("code", code)
                .setParameter("companyId", companyId)
                .executeUpdate();
    }

    protected void detach() {
        entityManager.flush();
        entityManager.clear();
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }
}
