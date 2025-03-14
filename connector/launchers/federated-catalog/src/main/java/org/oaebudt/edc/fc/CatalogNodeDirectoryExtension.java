package org.oaebudt.edc.fc;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;

public class CatalogNodeDirectoryExtension implements ServiceExtension {

    @Provider 
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        return new CatalogNodeDirectory();
    }

}
