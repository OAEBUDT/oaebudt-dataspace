package org.oaebudt.edc.report;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;

import static org.eclipse.edc.web.spi.configuration.ApiContext.PUBLIC;
import static org.oaebudt.edc.report.ReportAuthenticationExtension.NAME;

@Extension(NAME)
public class ReportAuthenticationExtension implements ServiceExtension {
    public static final String NAME = "Report API";


    private static final String REPORT = "report";

    @Configuration
    private KeycloakConfiguration keycloakConfiguration;

    @Configuration
    private ReportApiConfiguration reportApiConfiguration;

    @Inject
    private WebService webService;

    @Inject
    private ApiAuthenticationRegistry authenticationRegistry;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private PortMappingRegistry portMappingRegistry;


    @Override
    public void initialize(final ServiceExtensionContext context) {

        portMappingRegistry.register(new PortMapping(REPORT, reportApiConfiguration.port, reportApiConfiguration.path()));
        webService.registerResource(REPORT, new ReportApiController());
    }

    @Settings
    record KeycloakConfiguration(
            @Setting(key = "web.http.auth.jwk.url", description = "Url for for getting keycloak public keys", defaultValue = "no:op")
            String jwkUrl
    ) {

    }

    @Settings
    record ReportApiConfiguration(
            @Setting(key = "web.http." + REPORT + ".port", description = "Port for " + REPORT + " api context", defaultValue = "no:op")
            int port,
            @Setting(key = "web.http." + REPORT + ".path", description = "Path for " + REPORT + " api context", defaultValue = "no:op")
            String path
    ) {

    }

}
