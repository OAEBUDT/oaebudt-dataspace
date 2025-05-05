package org.oaebudt.edc.core;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.oaebudt.edc.core.store.InMemoryParticipantGroupStore;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

@Extension(OaebudtCoreExtension.NAME)
public class OaebudtCoreExtension implements ServiceExtension {
    public static final String NAME = "Core Extension";

    @Override
    public String name() {
        return NAME;
    }

    @Provider(isDefault = true)
    public ParticipantGroupStore defaultParticipantStore() {
        return new InMemoryParticipantGroupStore();
    }

}
