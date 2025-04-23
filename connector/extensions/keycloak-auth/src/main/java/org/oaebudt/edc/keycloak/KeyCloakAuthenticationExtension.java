package org.oaebudt.edc.keycloak;

import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationProviderRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

public class KeyCloakAuthenticationExtension implements ServiceExtension {

    private static final String REPORT = "report";

    @Configuration
    private KeycloakConfiguration keycloakConfiguration;

    @Inject
    private WebService webService;

    @Inject
    private ApiAuthenticationProviderRegistry apiAuthenticationProviderRegistry;

    @Override
    public void initialize(final ServiceExtensionContext context) {

        apiAuthenticationProviderRegistry.register("keycloak",
                (config) -> Result.success(new KeycloakAuthenticationService(context.getMonitor(),
                keycloakConfiguration.jwkUrl())));

//        authenticationRegistry.register(MANAGEMENT, new KeycloakAuthenticationService(context.getMonitor(),
//                keycloakConfiguration.jwkUrl()));
//        authenticationRegistry.register(REPORT, new KeycloakAuthenticationService(context.getMonitor(),
//                keycloakConfiguration.jwkUrl()));
//
//        final var authenticationFilter = new AuthenticationRequestFilter(authenticationRegistry, MANAGEMENT);
//        final var reportAuthenticationFilter = new AuthenticationRequestFilter(authenticationRegistry, REPORT);
//
//        webService.registerResource(MANAGEMENT, authenticationFilter);
//        webService.registerResource(REPORT, reportAuthenticationFilter);
    }

    @Settings
    record KeycloakConfiguration(
            @Setting(key = "web.http.auth.jwk.url", description = "Url for for getting keycloak public keys", defaultValue = "no:op")
            String jwkUrl
    ) {

    }

}
