package dev.kalles.sale.security.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractSecurityApiContainerSupport {

    protected static final boolean DOCKER_AVAILABLE = isDockerAvailable();
    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            DOCKER_AVAILABLE ? new PostgreSQLContainer<>("postgres:17-alpine") : null;

    static {
        if (DOCKER_AVAILABLE) {
            POSTGRESQL_CONTAINER.start();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (DOCKER_AVAILABLE) {
            registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
            registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:security-testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        }

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("api.security.token.secret", () -> "security-spec-test-secret");
        registry.add("api.security.token.expiration", () -> "12");
        registry.add("cors.allowed-origin", () -> "http://localhost:3000");
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
