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

import static java.util.Map.entry;

import java.util.Map;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.vault.VaultContainer;

public class HashiCorpVaultEndToEndExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_IMAGE = "hashicorp/vault:latest";
    private final VaultContainer<?> vault;

    public HashiCorpVaultEndToEndExtension() {
        this(DEFAULT_IMAGE);
    }

    public HashiCorpVaultEndToEndExtension(final String dockerImageName) {
        vault = new VaultContainer<>(dockerImageName)
                .withVaultToken("root");
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        vault.stop();
        vault.close();
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        vault.start();
    }

    public Config config() {
        final var settings = Map.ofEntries(
                entry("edc.vault.hashicorp.url", "http://localhost:" + vault.getFirstMappedPort()),
                entry("edc.vault.hashicorp.token", "root"),
                entry("edc.vault.hashicorp.health.check.enabled", "false")
        );

        return ConfigFactory.fromMap(settings);
    }
}
