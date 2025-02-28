package oaebudt.dataspace.connector.utils;

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
