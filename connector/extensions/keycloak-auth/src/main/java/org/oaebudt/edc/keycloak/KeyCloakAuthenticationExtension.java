/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    }

    @Settings
    record KeycloakConfiguration(
            @Setting(key = "web.http.auth.jwk.url", description = "Url for for getting keycloak public keys", defaultValue = "no:op")
            String jwkUrl
    ) {

    }

}
