package org.oaebudt.edc.web.service;

import jakarta.json.JsonException;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oaebudt.edc.web.model.ReportType;
import org.oaebudt.edc.web.repository.ReportStore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportServiceTest {

    private ReportService reportService;
    private ReportStore reportStore;
    private Monitor monitor;
    private AssetService assetService;
    private ContractDefinitionService contractDefinitionService;
    private URI consumerApiBaseUrl;

    private static Stream<Arguments> invalidMetadataProvider() {
        return Stream.of(
                Arguments.of("{}", "legalOrganizationName is required"),
                Arguments.of("{\"legalOrganizationName\":\"\",\"countryOfOrganization\":\"US\",\"contactPerson\":\"Jane\",\"dataProcessingDescription\":\"desc\",\"qualityAssuranceMeasures\":\"qa\",\"dataLicensingTerms\":\"terms\",\"organizationWebsite\":\"http://valid.com\",\"contactEmail\":\"valid@example.com\",\"dataAccuracyLevel\":1,\"dataGenerationTransparencyLevel\":1,\"dataDeliveryReliabilityLevel\":1,\"dataFrequencyLevel\":1,\"dataGranularityLevel\":1,\"dataConsistencyLevel\":1}", "legalOrganizationName is required"),
                Arguments.of("{\"legalOrganizationName\":\"Org\",\"countryOfOrganization\":\"US\",\"contactPerson\":\"Jane\",\"dataProcessingDescription\":\"desc\",\"qualityAssuranceMeasures\":\"qa\",\"dataLicensingTerms\":\"terms\",\"organizationWebsite\":\"invalid-url\",\"contactEmail\":\"valid@example.com\",\"dataAccuracyLevel\":1,\"dataGenerationTransparencyLevel\":1,\"dataDeliveryReliabilityLevel\":1,\"dataFrequencyLevel\":1,\"dataGranularityLevel\":1,\"dataConsistencyLevel\":1}", "organizationWebsite must be a valid URL"),
                Arguments.of("{\"legalOrganizationName\":\"Org\",\"countryOfOrganization\":\"US\",\"contactPerson\":\"Jane\",\"dataProcessingDescription\":\"desc\",\"qualityAssuranceMeasures\":\"qa\",\"dataLicensingTerms\":\"terms\",\"organizationWebsite\":\"http://valid.com\",\"contactEmail\":\"invalid-email\",\"dataAccuracyLevel\":1,\"dataGenerationTransparencyLevel\":1,\"dataDeliveryReliabilityLevel\":1,\"dataFrequencyLevel\":1,\"dataGranularityLevel\":1,\"dataConsistencyLevel\":1}", "contactEmail must be a valid email address"),
                Arguments.of("{\"legalOrganizationName\":\"Org\",\"countryOfOrganization\":\"US\",\"contactPerson\":\"Jane\",\"dataProcessingDescription\":\"desc\",\"qualityAssuranceMeasures\":\"qa\",\"dataLicensingTerms\":\"terms\",\"organizationWebsite\":\"http://valid.com\",\"contactEmail\":\"valid@example.com\",\"dataAccuracyLevel\":5,\"dataGenerationTransparencyLevel\":1,\"dataDeliveryReliabilityLevel\":1,\"dataFrequencyLevel\":1,\"dataGranularityLevel\":1,\"dataConsistencyLevel\":1}", "dataAccuracyLevel must be an integer between 1 and 3")
        );
    }

    private final String metadata = "{" +
            "        \"legalOrganizationName\": \"University Press Analytics\"," +
            "        \"countryOfOrganization\": \"United States\"," +
            "        \"organizationWebsite\": \"https://www.example-press.org\"," +
            "        \"contactPerson\": \"Jane Smith\"," +
            "        \"contactEmail\": \"jane.smith@example-press.org\"," +
            "        \"dataProcessingDescription\": \"Raw usage logs are processed using COUNTER Release 5 processing rules to filter robot and double-click activities. We use the open-source COUNTER-Robots library for robot detection.\"," +
            "        \"qualityAssuranceMeasures\": \"Monthly data validation process including outlier detection, completeness checking, and comparison with historical patterns. Automated and manual review processes are in place.\"," +
            "        \"dataLicensingTerms\": \"Data is provided under CC-BY license. Data recipients may use data for analysis and decision-making but must attribute the source.\"," +
            "        \"dataAccuracyLevel\": 3," +
            "        \"dataGenerationTransparencyLevel\": 2," +
            "        \"dataDeliveryReliabilityLevel\": 3," +
            "        \"dataFrequencyLevel\": 2," +
            "        \"dataGranularityLevel\": 2," +
            "        \"dataConsistencyLevel\": 2" +
            "    }";

    @BeforeEach
    void setUp() {
        reportStore = mock(ReportStore.class);
        monitor = mock(Monitor.class);
        assetService = mock(AssetService.class);
        contractDefinitionService = mock(ContractDefinitionService.class);
        consumerApiBaseUrl = URI.create("http://localhost:8181/api/");
        reportService = new ReportService(monitor, reportStore, assetService, contractDefinitionService, consumerApiBaseUrl);
    }

    @Test
    void uploadAndCreateAsset_shouldSaveReportAndCreateAsset() throws Exception {
        String json = "{\"data\": \"value\"}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        String title = "Test Report";
        String accessDefinition = "public";
        ReportType reportType = ReportType.ITEM_REPORT;

        when(assetService.create(any(Asset.class))).thenReturn(ServiceResult.success());
        when(contractDefinitionService.findById(anyString())).thenReturn(null);
        when(contractDefinitionService.create(any(ContractDefinition.class))).thenReturn(ServiceResult.success());

        reportService.uploadAndCreateAsset(inputStream, title, accessDefinition, metadata, reportType);

        verify(reportStore).saveReport(anyString(), eq(reportType));
        verify(assetService).create(any(Asset.class));
        verify(contractDefinitionService).create(any(ContractDefinition.class));
    }

    @Test
    void uploadAndCreateAsset_shouldThrowExceptionOnInvalidJson() {
        InputStream inputStream = new ByteArrayInputStream("invalid json".getBytes());
        String title = "Test Report";
        String accessDefinition = "public";
        ReportType reportType = ReportType.ITEM_REPORT;

        assertThrows(JsonException.class, () ->
                reportService.uploadAndCreateAsset(inputStream, title, accessDefinition, metadata, reportType));

    }

    @Test
    void uploadAndCreateAsset_shouldThrowExceptionWhenAccessDefinitionIsNull() {
        InputStream inputStream = new ByteArrayInputStream("{\"data\": \"value\"}".getBytes());
        String title = "Test Report";
        ReportType reportType = ReportType.ITEM_REPORT;

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                reportService.uploadAndCreateAsset(inputStream, title, null, metadata, reportType));

        assertEquals("Invalid access definition", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidMetadataProvider")
    void testInvalidMetadata(String jsonMetadata, String expectedErrorMessage) {
        String json = "{\"data\": \"value\"}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        String title = "Test Report";
        String accessDefinition = "public";
        ReportType reportType = ReportType.ITEM_REPORT;

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> reportService.uploadAndCreateAsset(inputStream, title, accessDefinition, jsonMetadata, reportType));

        assertTrue(thrown.getMessage().contains(expectedErrorMessage));
    }
}
