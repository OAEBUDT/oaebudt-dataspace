package org.oaebudt.edc.api;


import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class KeycloakAuthenticationService  implements ContainerRequestFilter {

    private static final String KEYCLOAK_JWKS_URL = "http://localhost:8080/realms/myrealm/protocol/openid-connect/certs";


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<String> authorization = requestContext.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if(!performCredentialValidation(authorization)) {
            throw new AuthenticationFailedException("error");
        }
    }

    public boolean isAuthenticated(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            var msg = "Headers were null or empty";
            throw new AuthenticationFailedException(msg);
        }

        var authHeaders = headers.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(HttpHeaders.AUTHORIZATION))
                .map(headers::get)
                .findFirst();

        return authHeaders.map(this::performCredentialValidation).orElseThrow(() -> {
            var msg = "Header '%s' not present";
            return new AuthenticationFailedException(msg.formatted(HttpHeaders.AUTHORIZATION));
        });
    }

    private boolean performCredentialValidation(List<String> authHeaders) {
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
            ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWSKeySelector<SimpleSecurityContext> keySelector = JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(URI.create(KEYCLOAK_JWKS_URL).toURL());
            jwtProcessor.setJWSKeySelector(keySelector);

            // Parse the token and process it
            SignedJWT signedJWT = SignedJWT.parse(token);
            jwtProcessor.process(signedJWT, null);

            // If we reach this point, the token is valid
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
