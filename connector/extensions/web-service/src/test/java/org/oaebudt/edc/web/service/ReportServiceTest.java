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

package org.oaebudt.edc.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        ObjectMapper mapper = new ObjectMapper();

        return Stream.of(
                Arguments.of(
                        buildMetadata(mapper, m -> m.remove("legalOrganizationName")),
                        "legalOrganizationName is required"
                ),
                Arguments.of(
                        buildMetadata(mapper, m -> m.put("countryOfOrganization", "")),
                        "countryOfOrganization is required"
                ),
                Arguments.of(
                        buildMetadata(mapper, m -> m.put("organizationWebsite", "invalid-url")),
                        "organizationWebsite must be a valid URL"
                ),
                Arguments.of(
                        buildMetadata(mapper, m -> m.put("contactEmail", "invalid-email")),
                        "contactEmail must be a valid email address"
                ),
                Arguments.of(
                        buildMetadata(mapper, m -> m.put("dataAccuracyLevel", 5)),
                        "dataAccuracyLevel must be an integer between 1 and 3"
                )
        );
    }

    private final String metadata = buildMetadata(new ObjectMapper(), m -> {});

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
    void uploadAndCreateAsset_shouldSaveReportAndCreateAsset() {
        String json = "{\"data\": \"value\"}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        String title = "Test Report";
        String accessDefinition = "public";
        ReportType reportType = ReportType.ITEM_REPORT;

        when(assetService.create(any(Asset.class))).thenReturn(ServiceResult.success());
        when(contractDefinitionService.findById(anyString())).thenReturn(null);
        when(contractDefinitionService.create(any(ContractDefinition.class))).thenReturn(ServiceResult.success());

        ServiceResult<String> result = reportService.uploadAndCreateAsset(inputStream, title, accessDefinition, metadata, reportType);

        verify(reportStore).saveReport(anyString(), eq(reportType));
        verify(assetService).create(any(Asset.class));
        verify(contractDefinitionService).create(any(ContractDefinition.class));
        Assertions.assertThat(result.succeeded()).isTrue();
        Assertions.assertThat(result.getContent()).startsWith("ITEM_REPORT");
    }

    @Test
    void uploadAndCreateAsset_shouldThrowExceptionOnInvalidJson() {
        InputStream inputStream = new ByteArrayInputStream("invalid json".getBytes());
        String title = "Test Report";
        String accessDefinition = "public";
        ReportType reportType = ReportType.ITEM_REPORT;

        ServiceResult<String> result = reportService.uploadAndCreateAsset(inputStream, title, accessDefinition, metadata, reportType);
        assertTrue(result.failed());
        assertTrue(result.getFailureDetail().contains("Invalid report json:"));
    }

    @Test
    void uploadAndCreateAsset_shouldThrowExceptionWhenAccessDefinitionIsNull() {
        InputStream inputStream = new ByteArrayInputStream("{\"data\": \"value\"}".getBytes());
        String title = "Test Report";
        ReportType reportType = ReportType.ITEM_REPORT;

        ServiceResult<String> result = reportService.uploadAndCreateAsset(inputStream, title, null, metadata, reportType);

        assertTrue(result.failed());
        assertEquals("Invalid access definition", result.getFailureDetail());
    }

    @ParameterizedTest
    @MethodSource("invalidMetadataProvider")
    void testInvalidMetadata(String jsonMetadata, String expectedErrorMessage) {
        String json = "{\"data\": \"value\"}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        String title = "Test Report";
        String accessDefinition = "public";
        ReportType reportType = ReportType.ITEM_REPORT;

        ServiceResult<String> result = reportService.uploadAndCreateAsset(inputStream, title, accessDefinition, jsonMetadata, reportType);

        assertTrue(result.failed());
        assertTrue(result.getFailureDetail().contains(expectedErrorMessage));
    }

    private static String buildMetadata(ObjectMapper mapper, Consumer<ObjectNode> modifier) {
        ObjectNode metadata = mapper.createObjectNode();

        metadata.put("legalOrganizationName", "Org");
        metadata.put("countryOfOrganization", "US");
        metadata.put("contactPerson", "Jane");
        metadata.put("dataProcessingDescription", "desc");
        metadata.put("qualityAssuranceMeasures", "qa");
        metadata.put("dataLicensingTerms", "terms");
        metadata.put("organizationWebsite", "http://valid.com");
        metadata.put("contactEmail", "valid@example.com");
        metadata.put("dataAccuracyLevel", 1);
        metadata.put("dataGenerationTransparencyLevel", 1);
        metadata.put("dataDeliveryReliabilityLevel", 1);
        metadata.put("dataFrequencyLevel", 1);
        metadata.put("dataGranularityLevel", 1);
        metadata.put("dataConsistencyLevel", 1);

        modifier.accept(metadata);

        try {
            return mapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON metadata", e);
        }
    }
}
