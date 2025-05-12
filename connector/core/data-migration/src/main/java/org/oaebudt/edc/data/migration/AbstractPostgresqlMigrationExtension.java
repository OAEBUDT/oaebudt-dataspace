package org.oaebudt.edc.data.migration;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.sql.DriverManagerConnectionFactory;
import org.eclipse.edc.sql.datasource.ConnectionFactoryDataSource;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;

import java.util.Properties;

import static java.util.Optional.ofNullable;
import static org.flywaydb.core.api.MigrationVersion.LATEST;

@Extension(AbstractPostgresqlMigrationExtension.NAME)
public class AbstractPostgresqlMigrationExtension implements ServiceExtension {
    public static final String NAME = "Migration Extension";

    public static final String DEFAULT_SCHEMA = "public";

    private static final String PASSWORD = "password";
    private static final String USER = "user";

    @Inject
    private DataSourceRegistry dataSourceRegistry;

    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var config = context.getConfig();
        monitor = context.getMonitor().withPrefix("Migration");

        String url = config.getString("edc.datasource.default.url");
        String user = config.getString("edc.datasource.default.user");
        String password = config.getString("edc.datasource.default.password");

        var jdbcProperties = new Properties();

        // only set if not-null, otherwise Properties#add throws a NPE
        ofNullable(user).ifPresent(u -> jdbcProperties.put(USER, u));
        ofNullable(password).ifPresent(p -> jdbcProperties.put(PASSWORD, p));

        var driverManagerConnectionFactory = new DriverManagerConnectionFactory();
        var dataSource = new ConnectionFactoryDataSource(driverManagerConnectionFactory, url, jdbcProperties);

        var migrateResult = FlywayManager.migrate(dataSource, DEFAULT_SCHEMA, LATEST);

        if (!migrateResult.success) {
            throw new EdcPersistenceException(
                    String.format(
                            "Migrating DataSource %s failed: %s",
                            DataSourceRegistry.DEFAULT_DATASOURCE, String.join(", ", migrateResult.warnings)));
        }
        monitor.info("Migration Complete");
    }
}
