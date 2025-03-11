package org.oaebudt.edc.keycloak;

import static org.eclipse.edc.web.spi.configuration.ApiContext.MANAGEMENT;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

public class KeyCloakAuthenticationExtension implements ServiceExtension {

    @Configuration
    private KeycloakConfiguration keycloakConfiguration;

    @Inject
    private WebService webService;

    @Inject
    private ApiAuthenticationRegistry authenticationRegistry;

    @Override
    public void initialize(final ServiceExtensionContext context) {

        authenticationRegistry.register(MANAGEMENT, new KeycloakAuthenticationService(context.getMonitor(),
                keycloakConfiguration.jwkUrl()));

        final var authenticationFilter = new AuthenticationRequestFilter(authenticationRegistry, MANAGEMENT);

        webService.registerResource(MANAGEMENT, authenticationFilter);
    }

    @Settings
    record KeycloakConfiguration(
            @Setting(key = "web.http.auth.jwk.url", description = "Url for for getting keycloak public keys")
            String jwkUrl
    ) {

    }

}
