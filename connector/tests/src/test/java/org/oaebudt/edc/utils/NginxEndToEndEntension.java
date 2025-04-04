package org.oaebudt.edc.utils;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class NginxEndToEndEntension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_IMAGE = "nginx:1.27-alpine";
    private final NginxContainer<?> nginx;

    public NginxEndToEndEntension() {
        this(DEFAULT_IMAGE);
    }

    public NginxEndToEndEntension(final String dockerImageName) {
        nginx = new NginxContainer<>(dockerImageName);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        nginx.stop();
        nginx.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        nginx.setPortBindings(Collections.singletonList("19999:80"));
        nginx.start();

        Path tempFile = Files.createTempFile("did", ".json");

        nginx.execInContainer("mkdir", "-p", "/usr/share/nginx/html/.well-known");

        nginx.execInContainer("chmod", "755", "/usr/share/nginx/html/.well-known");

        Files.write(tempFile, getDidJson().getBytes(), StandardOpenOption.WRITE);

        nginx.copyFileToContainer(
                MountableFile.forHostPath(tempFile),
                "/usr/share/nginx/html/.well-known/did.json"
        );

        nginx.execInContainer("chmod", "644", "/usr/share/nginx/html/.well-known/did.json");
    }

    private String getDidJson() {
        return "{\"service\":[],\"verificationMethod\":[{\"id\":\"did:web:localhost%3A19999#key-1\",\"type\":\"" +
                "JsonWebKey2020\",\"controller\":\"did:web:localhost%3A19999\",\"publicKeyMultibase\":null,\"" +
                "publicKeyJwk\":{\"kty\":\"OKP\",\"crv\":\"Ed25519\",\"x\":\"" +
                "TgT6wICy9AkzvWlJlU1VPca5Qe7y1prX146REtKtOFA\"}}],\"authentication\":[\"key-1\"],\"id\":\"did:web:" +
                "localhost%3A19999\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"" +
                "did:web:localhost%3A19999\"}]}\n";
    }
}
