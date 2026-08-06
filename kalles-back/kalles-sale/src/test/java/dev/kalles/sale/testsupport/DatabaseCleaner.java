package dev.kalles.sale.testsupport;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trunca todas as tabelas de negocio entre cenarios de teste. Vive no classpath de teste,
 * mas e' descoberto pelo component scan da aplicacao (scanBasePackages = "dev.kalles"),
 * entao pode ser injetado tanto nas classes de suporte quanto nos steps do Cucumber.
 */
@Component
public class DatabaseCleaner {

    private static final String TABLE_QUERY = """
            SELECT tablename FROM pg_tables
            WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'
            """;

    private final DataSource dataSource;
    private volatile String truncateStatement;

    public DatabaseCleaner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void clean() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            if (truncateStatement == null) {
                truncateStatement = buildTruncateStatement(statement);
            }
            if (truncateStatement != null) {
                statement.execute(truncateStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to clean test database", e);
        }
    }

    private String buildTruncateStatement(Statement statement) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery(TABLE_QUERY)) {
            while (rs.next()) {
                tables.add(rs.getString("tablename"));
            }
        }
        if (tables.isEmpty()) {
            return null;
        }
        return tables.stream()
                .map(table -> "\"" + table + "\"")
                .collect(Collectors.joining(", ", "TRUNCATE TABLE ", " RESTART IDENTITY CASCADE"));
    }
}
