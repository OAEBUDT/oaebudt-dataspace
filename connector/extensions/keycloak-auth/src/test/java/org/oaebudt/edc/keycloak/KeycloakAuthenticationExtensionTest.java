package org.oaebudt.edc.keycloak;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationProviderRegistry;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@ExtendWith(DependencyInjectionExtension.class)
class KeycloakAuthenticationExtensionTest {

    private final WebService webService = mock();

    ApiAuthenticationProviderRegistry authenticationProviderRegistry = mock();

    @BeforeEach
    void setUp(final ServiceExtensionContext context) {
        context.registerService(WebService.class, webService);
        context.registerService(ApiAuthenticationProviderRegistry.class, authenticationProviderRegistry);
    }

    @Test
    void testInitialize(final KeyCloakAuthenticationExtension extension, final ServiceExtensionContext context) {
        extension.initialize(context);
        Mockito.verify(authenticationProviderRegistry).register(ArgumentMatchers.eq("keycloak"),
                any());
    }


}
