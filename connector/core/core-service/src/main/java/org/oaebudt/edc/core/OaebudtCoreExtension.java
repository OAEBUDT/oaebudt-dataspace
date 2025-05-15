package org.oaebudt.edc.core;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.DriverManagerConnectionFactory;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.datasource.ConnectionFactoryDataSource;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.oaebudt.edc.core.migration.FlywayManager;
import org.oaebudt.edc.core.store.SqlParticipantGroupStore;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Properties;

import static java.util.Optional.ofNullable;
import static org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry.DEFAULT_DATASOURCE;
import static org.flywaydb.core.api.MigrationVersion.LATEST;

@Extension(OaebudtCoreExtension.NAME)
public class OaebudtCoreExtension implements ServiceExtension {
    public static final String NAME = "Core Extension";

    public static final String DEFAULT_SCHEMA = "public";

    private static final String PASSWORD = "password";
    private static final String USER = "user";

    private Monitor monitor;

    @Inject
    private DataSourceRegistry dataSourceRegistry;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private TypeManager typeManager;

    @Inject
    private QueryExecutor queryExecutor;

    @Override
    public String name() {
        return NAME;
    }

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

    @Provider
    public ParticipantGroupStore createSqlDidEntryStore() {
        return new SqlParticipantGroupStore(dataSourceRegistry, DEFAULT_DATASOURCE, transactionContext,
                    typeManager.getMapper(), queryExecutor);
    }

}
