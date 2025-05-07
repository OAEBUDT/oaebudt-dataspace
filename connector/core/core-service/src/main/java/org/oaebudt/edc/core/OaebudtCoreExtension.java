package org.oaebudt.edc.core;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.oaebudt.edc.core.store.InMemoryParticipantGroupStore;
import org.oaebudt.edc.core.store.MongoParticipantGroupStore;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

@Extension(OaebudtCoreExtension.NAME)
public class OaebudtCoreExtension implements ServiceExtension {
    public static final String NAME = "Core Extension";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        ServiceExtension.super.initialize(context);
    }

    @Provider(isDefault = true)
    public ParticipantGroupStore inMemoryParticipantStore() {
        return new InMemoryParticipantGroupStore();
    }

    @Provider
    public ParticipantGroupStore defaultParticipantStore(ServiceExtensionContext context) {
        String dataSourceUrl = context.getSetting("web.datasource.mongo.url", "no:op");
        MongoClient client = MongoClients.create(dataSourceUrl);
        return new MongoParticipantGroupStore(client);
    }

}
