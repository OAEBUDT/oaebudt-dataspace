package org.oaebudt.edc.keycloak;


import com.nimbusds.common.contenttype.ContentType;
import jakarta.ws.rs.core.HttpHeaders;
import org.assertj.core.api.Assertions;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeycloakAuthenticationServiceTest {

    static KeycloakAuthenticationService authenticationService;

    @BeforeAll
    public static void setup() {
        final Monitor monitor = new ConsoleMonitor(ConsoleMonitor.Level.WARNING, true);
        final String jwksUrl = "";
        authenticationService = new KeycloakAuthenticationService(monitor, jwksUrl);
    }

    @Test
    void shouldFailToAuthenticateWithEmptyHeader() {
        Assertions.assertThatThrownBy(() ->
                authenticationService.isAuthenticated(Collections.emptyMap()))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Headers were null or empty");
    }

    @Test
    void shouldFailToAuthenticateWithInvalidToken() {
        Assertions.assertThatThrownBy(() ->
                        authenticationService.isAuthenticated(Map.of(HttpHeaders.AUTHORIZATION, List.of("Bearer invalidToken"))))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void shouldFailToAuthenticateWithoutAuthorizationHeader() {
        Assertions.assertThatThrownBy(() ->
                        authenticationService.isAuthenticated(Map.of(HttpHeaders.CONTENT_TYPE, List.of(ContentType.APPLICATION_JSON.toString()))))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Header 'Authorization' not present");
    }

    @Test
    void shouldFailToAuthenticateWithInvalidToken_NoBearerKeyword() {
        Assertions.assertThat(authenticationService.isAuthenticated(Map.of(HttpHeaders.AUTHORIZATION, List.of("invalidToken"))))
                        .isEqualTo(false);
    }

}
