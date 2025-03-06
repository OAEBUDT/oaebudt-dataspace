package org.oaebudt.edc.api;

import static org.eclipse.edc.web.spi.configuration.ApiContext.PUBLIC;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;

public class CustomProxyDataPlaneExtension implements ServiceExtension {

    private static final int DEFAULT_PUBLIC_PORT = 8185;
    private static final String DEFAULT_PUBLIC_PATH = "/api/public";

    @Configuration
    private PublicApiConfiguration apiConfiguration;
    @Setting(description = "Base url of the public API endpoint without the trailing slash. This should point to the public endpoint configured.",
            key = "edc.dataplane.proxy.public.endpoint")
    private String proxyPublicEndpoint;

    @Inject
    private PortMappingRegistry portMappingRegistry;

    @Inject
    private WebService webService;

    @Inject
    private ApiAuthenticationRegistry authenticationRegistry;


    @Override
    public void initialize(final ServiceExtensionContext context) {

        final var keyCloakAuthService = new KeycloakAuthenticationService();

        authenticationRegistry.register(PUBLIC, keyCloakAuthService);
        webService.registerResource(PUBLIC, new AuthenticationRequestFilter(authenticationRegistry, PUBLIC));
    }

    @Settings
    record PublicApiConfiguration(
            @Setting(key = "web.http." + PUBLIC + ".port",
                    description = "Port for " + PUBLIC + " api context",
                    defaultValue = DEFAULT_PUBLIC_PORT + "")
            int port,
            @Setting(key = "web.http." + PUBLIC + ".path",
                    description = "Path for " + PUBLIC + " api context",
                    defaultValue = DEFAULT_PUBLIC_PATH)
            String path
    ) {

    }
}
