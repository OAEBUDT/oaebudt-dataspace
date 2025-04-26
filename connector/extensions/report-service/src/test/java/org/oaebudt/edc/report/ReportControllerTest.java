package org.oaebudt.edc.report;


import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.catalog.spi.FederatedCatalogCache;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;


public class ReportControllerTest {

    private ReportStore reportStore;
    private Monitor monitor;
    private ReportApiController controller;
    private AssetService assetService;
    private ContractDefinitionService contractDefinitionService;
    private URI consumerApiBaseUrl;
    private FederatedCatalogCache federatedCatalogCache;

    @BeforeEach
    void setUp() {
        reportStore = mock(ReportStore.class);
        monitor = mock(Monitor.class);
        assetService = mock(AssetService.class);
        contractDefinitionService = mock(ContractDefinitionService.class);
        consumerApiBaseUrl = URI.create("Http://random-uri");
        federatedCatalogCache = mock(FederatedCatalogCache.class);
        controller = Mockito.spy(new ReportApiController(monitor, reportStore, assetService,contractDefinitionService, federatedCatalogCache, consumerApiBaseUrl));
    }

    @Test
    void uploadFile_validJson_savesToMongoAndReturnsCreated() {
        String title = "Test Report";
        ReportType reportType = ReportType.ITEM_REPORT;
        String jsonContent = "{\"field\":\"value\"}";
        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());

        when(assetService.create(any())).thenReturn(ServiceResult.success());
        when(contractDefinitionService.create(any(ContractDefinition.class))).thenReturn(ServiceResult.success());

        Response response = controller.uploadFile(inputStream, title, "require-dataprocessor", reportType);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("uploaded and saved"));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(reportStore).saveReport(jsonCaptor.capture(), eq(reportType));
        verify(assetService).create(any(Asset.class));

        JsonObject savedJson = Json.createReader(new ByteArrayInputStream(jsonCaptor.getValue().getBytes())).readObject();
        assertEquals("Test Report", savedJson.getString("title"));
        assertEquals(ReportType.ITEM_REPORT.name(), savedJson.getString("reportType"));
    }

    @Test
    void uploadFile_invalidJson_returnsBadRequest() {
        InputStream invalidJsonStream = new ByteArrayInputStream("invalid json".getBytes());

        Response response = controller.uploadFile(invalidJsonStream, "title", "require-dataprocessor", ReportType.ITEM_REPORT);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Failed to process uploaded JSON"));
        verify(monitor).warning(eq("Error uploading report"), any(JsonException.class));
    }

    @Test
    void uploadFile_unexpectedException_returnsServerError() {
        InputStream inputStream = new ByteArrayInputStream("{\"foo\":\"bar\"}".getBytes());
        Response response = controller.uploadFile(inputStream, "title", "require-dataprocessor", ReportType.TITLE_REPORT);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Something went wrong"));
        verify(monitor).warning(eq("Error uploading report"), any(RuntimeException.class));
    }

    @Test
    void uploadFile_InvalidAccessLevel_returnsBadRequest() {
        String title = "Test Report";
        ReportType reportType = ReportType.ITEM_REPORT;
        String jsonContent = "{\"field\":\"value\"}";
        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());

        when(assetService.create(any())).thenReturn(ServiceResult.success());
        when(contractDefinitionService.create(any(ContractDefinition.class))).thenReturn(ServiceResult.success());

        Response response = controller.uploadFile(inputStream, title, "random-accesslevel", reportType);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Policy id not found"));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verifyNoInteractions(reportStore, assetService);
    }
}
