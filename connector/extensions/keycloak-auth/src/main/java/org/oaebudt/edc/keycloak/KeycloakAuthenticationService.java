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

import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;


public class KeycloakAuthenticationService  implements AuthenticationService {

    private final Monitor monitor;
    private final String jwksPublicKey;

    public KeycloakAuthenticationService(final Monitor monitor, final String jwksPublicKeyUrl) {
        this.monitor = monitor;
        this.jwksPublicKey = jwksPublicKeyUrl;
    }

    @Override
    public boolean isAuthenticated(final Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            final var msg = "Headers were null or empty";
            monitor.warning(msg);
            throw new AuthenticationFailedException(msg);
        }

        final var authHeaders = headers.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(HttpHeaders.AUTHORIZATION))
                .map(headers::get)
                .findFirst();

        return authHeaders.map(this::performCredentialValidation).orElseThrow(() -> {
            final var msg = "Header '%s' not present";
            monitor.warning(msg);
            return new AuthenticationFailedException(msg.formatted(HttpHeaders.AUTHORIZATION));
        });
    }

    private boolean performCredentialValidation(final List<String> authHeaders) {
        if (Objects.isNull(authHeaders) || authHeaders.size() != 1) {
            return false;
        }
        var token = authHeaders.get(0);
        if (!token.toLowerCase().startsWith("bearer ")) {
            return false;
        }
        token = token.substring(6).trim();

        try {

            // Set up the JWT processor
            final ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            final JWSKeySelector<SimpleSecurityContext> keySelector =
                    JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(URI.create(jwksPublicKey).toURL());
            jwtProcessor.setJWSKeySelector(keySelector);

            // Parse the token and process it
            final SignedJWT signedJWT = SignedJWT.parse(token);
            jwtProcessor.process(signedJWT, null);

            // If we reach this point, the token is valid
            return true;
        } catch (final Exception e) {
            throw new AuthenticationFailedException("Invalid token");
        }

    }
}
