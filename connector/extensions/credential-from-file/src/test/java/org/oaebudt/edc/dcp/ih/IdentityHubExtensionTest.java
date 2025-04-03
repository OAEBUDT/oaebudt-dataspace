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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class IdentityHubExtensionTest {

    private IdentityHubExtension extension;
    private CredentialStore storeMock;
    private TypeManager typeManagerMock;
    private ServiceExtensionContext contextMock;
    private Monitor monitorMock;
    private Config configMock;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storeMock = mock(CredentialStore.class);
        typeManagerMock = mock(TypeManager.class);
        contextMock = mock(ServiceExtensionContext.class);
        monitorMock = mock(Monitor.class);
        configMock = mock(Config.class);

        extension = new IdentityHubExtension();
        extension.store = storeMock;
        extension.typeManager = typeManagerMock;

        when(contextMock.getMonitor()).thenReturn(monitorMock);
        when(contextMock.getConfig()).thenReturn(configMock);
    }

    @Test
    void initialize_shouldSetCredentialsDirAndMonitor() {
        String testPath = "/test/path";
        when(configMock.getString("edc.did.credentials.path")).thenReturn(testPath);

        extension.initialize(contextMock);

        assertThat(extension.credentialsDir).isEqualTo(testPath);
        verify(monitorMock).withPrefix("VC-STORAGE");
    }

    @Test
    void start_shouldSeedCredentials() throws IOException {
        // Create a test JSON file in temp directory
        Path credentialFile = tempDir.resolve("test.json");
        Files.writeString(credentialFile, "{\"test\":\"value\"}");

        extension.credentialsDir = tempDir.toString();
        extension.monitor = monitorMock;

        // Mock TypeManager behavior
        VerifiableCredentialResource mockResource = mock(VerifiableCredentialResource.class);
        when(typeManagerMock.getMapper(anyString())).thenReturn(new TestObjectMapper(mockResource));

        extension.start();

        // Verify the credential was stored
        verify(storeMock).create(mockResource);
        verify(monitorMock).debug(contains("Stored VC from file"));
    }

    @Test
    void start_shouldHandleMissingDirectory() {
        extension.credentialsDir = "/nonexistent/path";
        extension.monitor = monitorMock;

        extension.start();

        verify(monitorMock).warning(contains("does not exist"));
        verify(storeMock, never()).create(any());
    }

    @Test
    void start_shouldHandleEmptyDirectory() throws IOException {
        // Create an empty directory
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);

        extension.credentialsDir = emptyDir.toString();
        extension.monitor = monitorMock;

        extension.start();

        verify(monitorMock).warning(contains("No files found in directory"));
        verify(storeMock, never()).create(any());
    }

    @Test
    void start_shouldHandleIOException() throws IOException {
        // Create a test JSON file in temp directory
        Path credentialFile = tempDir.resolve("test.json");
        Files.writeString(credentialFile, "{\"test\":\"value\"}");

        extension.credentialsDir = tempDir.toString();
        extension.monitor = monitorMock;

        // Mock TypeManager to throw IOException when reading the file
        when(typeManagerMock.getMapper(anyString())).thenReturn(new TestObjectMapperWithException());

        extension.start();

        verify(monitorMock).severe(contains("Error storing VC"), any(IOException.class));
    }

    // Test ObjectMapper that returns a mock resource
    private static class TestObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {
        private final VerifiableCredentialResource mockResource;

        TestObjectMapper(VerifiableCredentialResource mockResource) {
            this.mockResource = mockResource;
        }

        @Override
        public <T> T readValue(File src, Class<T> valueType) throws IOException {
            return (T) mockResource;
        }
    }

    // Test ObjectMapper that throws IOException
    private static class TestObjectMapperWithException extends com.fasterxml.jackson.databind.ObjectMapper {
        @Override
        public <T> T readValue(File src, Class<T> valueType) throws IOException {
            throw new IOException("Test error");
        }
    }
}
