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

class KeycloakAuthenticationServiceTest {

    static KeycloakAuthenticationService authenticationService;

    @BeforeAll
    static void setup() {
        final Monitor monitor = new ConsoleMonitor(ConsoleMonitor.Level.WARNING, true);
        final String jwksUrl = "";
        authenticationService = new KeycloakAuthenticationService(monitor, jwksUrl);
    }

    @Test
    void shouldFailToAuthenticateWithEmptyHeader() {
        Map<String, List<String>> headers = Collections.emptyMap();
        Assertions.assertThatThrownBy(() ->
                authenticationService.isAuthenticated(headers))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Headers were null or empty");
    }

    @Test
    void shouldFailToAuthenticateWithInvalidToken() {
        Map<String, List<String>> headers = Map.of(HttpHeaders.AUTHORIZATION, List.of("Bearer invalidToken"));
        Assertions.assertThatThrownBy(() ->
                        authenticationService.isAuthenticated(headers))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void shouldFailToAuthenticateWithoutAuthorizationHeader() {
        Map<String, List<String>> headers = Map.of(HttpHeaders.CONTENT_TYPE, List.of(ContentType.APPLICATION_JSON.toString()));
        Assertions.assertThatThrownBy(() ->
                        authenticationService.isAuthenticated(headers))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Header 'Authorization' not present");
    }

    @Test
    void shouldFailToAuthenticateWithInvalidToken_NoBearerKeyword() {
        Assertions.assertThat(authenticationService.isAuthenticated(Map.of(HttpHeaders.AUTHORIZATION, List.of("invalidToken"))))
                        .isFalse();
    }

}
