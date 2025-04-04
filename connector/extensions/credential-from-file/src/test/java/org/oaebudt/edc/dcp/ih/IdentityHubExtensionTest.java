package org.oaebudt.edc.dcp.ih;

import org.eclipse.edc.identityhub.spi.verifiablecredentials.model.VerifiableCredentialResource;
import org.eclipse.edc.identityhub.spi.verifiablecredentials.store.CredentialStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdentityHubExtensionTest {

    private static final String TEST_CREDENTIALS_PATH = "test/credentials/path";
    private static final String SAMPLE_VC_JSON = "{\"id\":\"test-vc-id\", \"type\":\"VerifiableCredential\"}";

    private IdentityHubExtension extension;
    private CredentialStore credentialStore;
    private TypeManager typeManager;
    private ServiceExtensionContext context;
    private Monitor monitor;
    private Config config;

    @BeforeEach
    void setUp() {
        extension = new IdentityHubExtension();
        credentialStore = mock(CredentialStore.class);
        typeManager = mock(TypeManager.class);
        context = mock(ServiceExtensionContext.class);
        monitor = mock(Monitor.class);
        config = mock(Config.class);

        extension.store = credentialStore;
        extension.typeManager = typeManager;

        when(context.getConfig()).thenReturn(config);
        when(config.getString("edc.did.credentials.path")).thenReturn(TEST_CREDENTIALS_PATH);
        when(context.getMonitor()).thenReturn(monitor);
        when(monitor.withPrefix(anyString())).thenReturn(monitor);
    }

    @Test
    void initialize_shouldSetCredentialsPathAndMonitor() {
        extension.initialize(context);

        assertThat(extension.credentialsDir).isEqualTo(TEST_CREDENTIALS_PATH);
        assertThat(extension.monitor).isEqualTo(monitor);
    }

    @Test
    void start_shouldSeedCredentials(@TempDir Path tempDir) throws IOException {
        File jsonFile = new File(tempDir.toFile(), "test.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(SAMPLE_VC_JSON);
        }

        var objectMapper = mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        when(typeManager.getMapper(JSON_LD)).thenReturn(objectMapper);

        var vcResource = mock(VerifiableCredentialResource.class);
        when(objectMapper.readValue(any(File.class), eq(VerifiableCredentialResource.class))).thenReturn(vcResource);

        extension.initialize(context);
        extension.credentialsDir = tempDir.toString();
        extension.start();

        verify(credentialStore).create(vcResource);
    }

    @Test
    void start_withNonExistentDirectory_shouldNotCreateCredentials() {
        extension.initialize(context);
        extension.credentialsDir = "/non/existent/path/" + System.currentTimeMillis();
        extension.start();

        verify(credentialStore, never()).create(any());
    }

    @Test
    void start_withEmptyDirectory_shouldNotCreateCredentials(@TempDir Path tempDir) {
        extension.initialize(context);
        extension.credentialsDir = tempDir.toString();
        extension.start();

        verify(credentialStore, never()).create(any());
    }

    @Test
    void start_shouldHandleFileReadErrors(@TempDir Path tempDir) throws IOException {
        File jsonFile = new File(tempDir.toFile(), "test.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(SAMPLE_VC_JSON);
        }

        extension.initialize(context);
        extension.credentialsDir = tempDir.toString();

        var objectMapper = mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        when(typeManager.getMapper(JSON_LD)).thenReturn(objectMapper);
        when(objectMapper.readValue(any(File.class), eq(VerifiableCredentialResource.class)))
                .thenThrow(new IOException("Test file read error"));

        extension.start();

        verify(credentialStore, never()).create(any());
    }

    @Test
    void start_shouldFilterNonJsonFiles(@TempDir Path tempDir) throws IOException {
        File jsonFile = new File(tempDir.toFile(), "test.json");
        File txtFile = new File(tempDir.toFile(), "test.txt");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(SAMPLE_VC_JSON);
        }
        try (FileWriter writer = new FileWriter(txtFile)) {
            writer.write("This is not a JSON file");
        }

        extension.initialize(context);
        extension.credentialsDir = tempDir.toString();

        var objectMapper = mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        when(typeManager.getMapper(JSON_LD)).thenReturn(objectMapper);

        var vcResource = mock(VerifiableCredentialResource.class);
        when(objectMapper.readValue(any(File.class), eq(VerifiableCredentialResource.class))).thenReturn(vcResource);

        extension.start();

        verify(objectMapper, times(1)).readValue(any(File.class), eq(VerifiableCredentialResource.class));
        verify(credentialStore, times(1)).create(any());
    }
}
