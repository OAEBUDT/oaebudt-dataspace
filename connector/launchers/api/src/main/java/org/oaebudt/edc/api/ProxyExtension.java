package org.oaebudt.edc.api;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

import static org.eclipse.edc.web.spi.configuration.ApiContext.PUBLIC;

public class ProxyExtension implements ServiceExtension {

    @Inject
    private WebService webService;

    @Override
    public void initialize(ServiceExtensionContext context) {

        webService.registerResource(PUBLIC, new ProxyController());
    }
}
