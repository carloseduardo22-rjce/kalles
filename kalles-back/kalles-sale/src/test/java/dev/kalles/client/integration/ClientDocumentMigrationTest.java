package dev.kalles.client.integration;

import dev.kalles.testsupport.SharedPostgresContainer;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@DisplayName("V58 converte o CPF do cliente em documento")
class ClientDocumentMigrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.instance();
    private static final String TENANT_ID = "123e4567-e89b-12d3-a456-426614174000";

    private final UUID companyId = UUID.randomUUID();
    private String url;

    @BeforeEach
    void createEmptyDatabase() throws SQLException {
        String database = "v58_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = connect(POSTGRES.getJdbcUrl());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + database);
        }
        url = POSTGRES.getJdbcUrl().replace("/" + POSTGRES.getDatabaseName(), "/" + database);
        migrateTo("57");
        execute("INSERT INTO company (id, name, tenant_id) VALUES ('" + companyId + "', 'Loja do teste de migração', '" + TENANT_ID + "')");
    }

    @Test
    @DisplayName("remove a pontuação do CPF existente")
    void removesPunctuationFromExistingCpf() throws SQLException {
        insertClient("123.456.789-09");

        migrateTo("58");

        assertEquals("12345678909", singleDocument());
    }

    @Test
    @DisplayName("para com mensagem clara quando há cliente sem CPF")
    void stopsWhenAClientHasNoCpf() throws SQLException {
        insertClient(null);

        FlywayException error = assertThrows(FlywayException.class, () -> migrateTo("58"));

        assertTrue(error.getMessage().contains("1 client(s) without CPF"));
    }

    @Test
    @DisplayName("para com mensagem clara quando o CPF se repete sem pontuação")
    void stopsWhenCpfRepeatsOncePunctuationIsRemoved() throws SQLException {
        insertClient("123.456.789-09");
        insertClient("12345678909");

        FlywayException error = assertThrows(FlywayException.class, () -> migrateTo("58"));

        assertTrue(error.getMessage().contains("1 CPF(s) duplicated per company"));
    }

    private void migrateTo(String version) {
        Flyway.configure()
                .dataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target(version)
                .load()
                .migrate();
    }

    private void insertClient(String cpf) throws SQLException {
        String value = cpf == null ? "NULL" : "'" + cpf + "'";
        execute("INSERT INTO client (name, cpf, company_id) VALUES ('Cliente', " + value + ", '" + companyId + "')");
    }

    private String singleDocument() throws SQLException {
        try (Connection connection = connect(url);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT document FROM client")) {
            result.next();
            return result.getString(1);
        }
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = connect(url);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private Connection connect(String jdbcUrl) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
