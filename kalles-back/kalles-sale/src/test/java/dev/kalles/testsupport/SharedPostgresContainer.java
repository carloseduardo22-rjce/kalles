package dev.kalles.testsupport;

import org.testcontainers.containers.PostgreSQLContainer;

public final class SharedPostgresContainer {

    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        INSTANCE.start();
    }

    private SharedPostgresContainer() {
    }

    public static PostgreSQLContainer<?> instance() {
        return INSTANCE;
    }
}
