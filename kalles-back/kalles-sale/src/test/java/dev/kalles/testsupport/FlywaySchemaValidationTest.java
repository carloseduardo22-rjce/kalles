package dev.kalles.testsupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Schema do Flyway confere com as entidades JPA")
class FlywaySchemaValidationTest {

    private static final PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.instance();

    @Autowired
    private Environment environment;

    @DynamicPropertySource
    static void validateAgainstMigrations(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        registry.add("api.security.token.secret", () -> "schema-validation-test-secret");
        registry.add("cors.allowed-origin", () -> "http://localhost:3000");
    }

    @Test
    @DisplayName("toda entidade mapeia uma tabela que as migrations criam")
    void everyEntityMapsATableTheMigrationsCreate() {
        assertEquals("validate", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
    }
}
