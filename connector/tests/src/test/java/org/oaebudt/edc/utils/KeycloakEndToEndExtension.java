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

package org.oaebudt.edc.utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.util.Collections;
import java.util.Map;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;


public class KeycloakEndToEndExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_IMAGE = "quay.io/keycloak/keycloak:latest";
    private static final String REALM_NAME = "test-realm";
    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";

    private final KeycloakContainer keycloakContainer;
    private Keycloak adminClient;


    public KeycloakEndToEndExtension() {
        this(DEFAULT_IMAGE);
    }

    public KeycloakEndToEndExtension(final String dockerImageName) {
        this.keycloakContainer =  new KeycloakContainer(dockerImageName);
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        keycloakContainer.stop();
        keycloakContainer.close();
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        keycloakContainer.start();
        setupRealm();
        setupUser();
    }

    private void setupRealm() {

        adminClient = KeycloakBuilder.builder()
                .serverUrl(keycloakContainer.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();

        final RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(REALM_NAME);
        realm.setAccessTokenLifespan(3600); //1 hour
        realm.setEnabled(true);

        // Create client
        final ClientRepresentation client = new ClientRepresentation();
        client.setClientId(CLIENT_ID);
        client.setSecret(CLIENT_SECRET);
        client.setDirectAccessGrantsEnabled(true);
        client.setPublicClient(false);
        client.setServiceAccountsEnabled(true);

        realm.setClients(Collections.singletonList(client));

        adminClient.realms().create(realm);

    }

    private void setupUser() {
        // Create test user
        final UserRepresentation user = new UserRepresentation();
        user.setUsername(TEST_USER);
        user.setFirstName("First Name");
        user.setLastName("Last name");
        user.setEmailVerified(true);
        user.setEmail("random@connector.com");
        user.setEnabled(true);

        adminClient.realm(REALM_NAME).users().create(user);

        // Set user password
        final String userId = adminClient.realm(REALM_NAME).users().search(TEST_USER).get(0).getId();
        final CredentialRepresentation password = new CredentialRepresentation();
        password.setType(CredentialRepresentation.PASSWORD);
        password.setValue(TEST_PASSWORD);
        password.setTemporary(false);
        adminClient.realm(REALM_NAME).users().get(userId).resetPassword(password);
    }

    public String getToken() {

        final Keycloak keycloakUser = KeycloakBuilder.builder()
                .serverUrl(keycloakContainer.getAuthServerUrl())
                .realm(REALM_NAME)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .grantType(OAuth2Constants.PASSWORD)
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .build();

        return keycloakUser.tokenManager().getAccessTokenString();
    }

    public String getJwksUrl() {
        return keycloakContainer.getAuthServerUrl() + "/realms/" + REALM_NAME + "/protocol/openid-connect/certs";
    }

    public Config config() {
        final var settings = Map.of("web.http.auth.jwk.url", this.getJwksUrl());

        return ConfigFactory.fromMap(settings);
    }
}
