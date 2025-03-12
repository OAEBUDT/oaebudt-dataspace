package org.oaebudt.edc.keycloak;

import static org.mockito.Mockito.mock;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.ApiContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@ExtendWith(DependencyInjectionExtension.class)
public class KeycloakAuthenticationExtensionTest {

    private final WebService webService = mock();

    ApiAuthenticationRegistry authenticationRegistry = mock();

    @BeforeEach
    void setUp(final ServiceExtensionContext context) {
        context.registerService(WebService.class, webService);
        context.registerService(ApiAuthenticationRegistry.class, authenticationRegistry);
    }

    @Test
    void testInitialize(final KeyCloakAuthenticationExtension extension, final ServiceExtensionContext context) {
        extension.initialize(context);
        Mockito.verify(authenticationRegistry).register(ArgumentMatchers.eq(ApiContext.MANAGEMENT),
                ArgumentMatchers.any(KeycloakAuthenticationService.class));
        Mockito.verify(webService).registerResource(ArgumentMatchers.eq(ApiContext.MANAGEMENT),
                ArgumentMatchers.any(AuthenticationRequestFilter.class));
    }


}
