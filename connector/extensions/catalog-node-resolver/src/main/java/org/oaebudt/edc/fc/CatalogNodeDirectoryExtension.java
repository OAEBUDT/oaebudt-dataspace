package org.oaebudt.edc.fc;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;

import java.util.Arrays;

public class CatalogNodeDirectoryExtension implements ServiceExtension {

    @Configuration
    private FcParticipantsListConfiguration fcParticipantsListConfiguration;

    @Provider 
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        return new CatalogNodeDirectory(Arrays.asList(fcParticipantsListConfiguration.participants().split(",")));
    }

    @Settings
    record FcParticipantsListConfiguration(
            @Setting(key = "fc.participants.list", description = "List of participants to crawl", defaultValue = "no:op")
            String participants
    ) {

    }

}
