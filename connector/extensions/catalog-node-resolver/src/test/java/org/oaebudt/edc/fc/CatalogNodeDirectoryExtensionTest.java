package org.oaebudt.edc.fc;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DependencyInjectionExtension.class)
public class CatalogNodeDirectoryExtensionTest {

    @Test
    void testInitializeProvider(final CatalogNodeDirectoryExtension extension, final ServiceExtensionContext context) {
        extension.initialize(context);
        Assertions.assertThat(extension.federatedCacheNodeDirectory()).isInstanceOf(CatalogNodeDirectory.class);
    }
}
