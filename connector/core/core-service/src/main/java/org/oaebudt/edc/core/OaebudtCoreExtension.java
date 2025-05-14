package org.oaebudt.edc.core;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.oaebudt.edc.core.store.SqlParticipantGroupStore;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import static org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry.DEFAULT_DATASOURCE;

@Extension(OaebudtCoreExtension.NAME)
public class OaebudtCoreExtension implements ServiceExtension {
    public static final String NAME = "Core Extension";

    @Inject
    private DataSourceRegistry dataSourceRegistry;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private TypeManager typeManager;

    @Inject
    private QueryExecutor queryExecutor;

    private ParticipantGroupStore store;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public ParticipantGroupStore createSqlDidEntryStore() {
        if (store == null) {
            store = new SqlParticipantGroupStore(dataSourceRegistry, DEFAULT_DATASOURCE, transactionContext,
                    typeManager.getMapper(), queryExecutor);
        }
        return store;
    }

}
