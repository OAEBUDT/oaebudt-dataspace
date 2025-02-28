package oaebudt.dataspace.connector.utils;

import static java.lang.String.format;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;


public class PostgresEndToEndExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_IMAGE = "postgres";

    private final PostgreSQLContainer<?> postgres;

    public PostgresEndToEndExtension() {
        this(DEFAULT_IMAGE);
    }

    public PostgresEndToEndExtension(final String dockerImageName) {
        postgres = new PostgreSQLContainer<>(dockerImageName);
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        postgres.stop();
        postgres.close();
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        postgres.start();
    }

    public Config configFor(final String databaseName) {
        final var settings = Map.of(
                "edc.datasource.default.url", "jdbc:postgresql://%s:%d/%s"
                        .formatted(postgres.getHost(), postgres.getFirstMappedPort(), databaseName),
                "edc.datasource.default.user", postgres.getUsername(),
                "edc.datasource.default.password", postgres.getPassword(),
                "edc.sql.schema.autocreate", "true"
        );

        return ConfigFactory.fromMap(settings);
    }

    public void createDatabase(final String name) {

        final var jdbcUrl = postgres.getJdbcUrl() + postgres.getDatabaseName();
        try (var connection = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword())) {
            connection.createStatement().execute(format("create database %s;", name));
        } catch (final SQLException e) {
            // database could already exist
        }
    }
}
