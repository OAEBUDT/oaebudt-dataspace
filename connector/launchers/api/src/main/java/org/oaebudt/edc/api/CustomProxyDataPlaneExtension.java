package org.oaebudt.edc.api;

import static org.eclipse.edc.web.spi.configuration.ApiContext.MANAGEMENT;
import static org.eclipse.edc.web.spi.configuration.ApiContext.PUBLIC;

//import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
//import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.token.spi.TokenValidationRulesRegistry;
import org.eclipse.edc.token.spi.TokenValidationService;
import org.eclipse.edc.web.spi.WebService;

public class CustomProxyDataPlaneExtension implements ServiceExtension {

    @Inject
    private WebService webService;

//    @Inject
//    private ApiAuthenticationRegistry authenticationRegistry;

    @Override
    public void initialize(final ServiceExtensionContext context) {


        final var keyCloakAuthService = new KeycloakAuthenticationService();

//        authenticationRegistry.register(PUBLIC, keyCloakAuthService);
        webService.registerResource(MANAGEMENT, keyCloakAuthService);
    }

    @Provider(isDefault = true)
    public TokenValidationService tokenValidationService() {
        return ((tokenRepresentation, publicKeyResolver, rules) -> null);
    }

}
