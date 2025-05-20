package org.oaebudt.edc.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.connector.dataplane.http.spi.HttpDataAddress;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.oaebudt.edc.web.dto.CreateAssetRequest;
import org.oaebudt.edc.web.model.ReportType;
import org.oaebudt.edc.web.repository.ReportStore;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

public class ReportService {
    private final ReportStore reportStore;
    private final Monitor monitor;
    private final AssetService assetService;
    private final ContractDefinitionService contractDefinitionService;
    private final URI consumerApiBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportService(Monitor monitor, ReportStore reportStore, AssetService assetService,
                         ContractDefinitionService contractDefinitionService, URI consumerApiBaseUrl) {
        this.monitor = monitor;
        this.reportStore = reportStore;
        this.assetService = assetService;
        this.contractDefinitionService = contractDefinitionService;
        this.consumerApiBaseUrl = consumerApiBaseUrl;
    }

    public void createAsset(CreateAssetRequest request) {
        validateCreateAsset(request);
        MetadataValidator.validate(request.metadata());

        createAssetInConnector(
                request.title(),
                request.metadata(),
                request.reportType(),
                request.accessDefinition(),
                request.url(),
                request.method(),
                request.authHeaderKey(),
                request.authCode(),
                request.headers()
        );
    }

    public void uploadAndCreateAsset(InputStream file, String title, String accessDefinition, String metadataJson, ReportType reportType) throws JsonProcessingException {
        JsonReader reader = Json.createReader(file);
        if (Objects.isNull(accessDefinition)) {
            throw new IllegalArgumentException("Invalid access definition");
        }

        Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {});
        MetadataValidator.validate(metadata);

        JsonObject jsonObject = reader.readObject();
        JsonObject enriched = Json.createObjectBuilder(jsonObject)
                .add("title", title)
                .add("reportType", reportType.name())
                .build();

        reportStore.saveReport(enriched.toString(), reportType);

        String uri = consumerApiBaseUrl.resolve("report?reportType=" + reportType.name()).toString();
        createAssetInConnector(title, metadata, reportType, accessDefinition, uri, "GET", null, null, Collections.emptyMap());
    }

    private void createAssetInConnector(String title, Map<String, Object> assetMetadata, ReportType reportType,
                                        String accessDefinition, String uri, String method,
                                        String authKey, String authCode, Map<String, Object> headers) {

        DataAddress dataAddress = HttpDataAddress.Builder.newInstance()
                .type("HttpData")
                .baseUrl(uri)
                .authKey(authKey)
                .authCode(authCode)
                .method(method)
                .properties(headers)
                .build();

        String assetId = reportType.name();

        Asset asset = Asset.Builder.newInstance()
                .id(assetId)
                .properties(Map.of(
                        "name", title,
                        "contentType", "application/json",
                        "type", reportType.name()
                ))
                .properties(assetMetadata)
                .dataAddress(dataAddress)
                .build();

        assetService.create(asset)
                .onSuccess(pd -> monitor.info("Asset '%s' created".formatted(reportType.name())))
                .onFailure(failure -> {
                    if (failure.getReason() == ServiceFailure.Reason.CONFLICT) {
                        monitor.info("Asset already exists: %s".formatted(reportType.name()));
                    } else {
                        monitor.warning("Failed to create asset: %s".formatted(failure.getReason()));
                    }
                });

        createContractDefinition(accessDefinition + "-" + reportType.name(), accessDefinition, assetId);
    }

    private void createContractDefinition(String contractDefinitionId, String policyId, String assetId) {
        if (contractDefinitionService.findById(contractDefinitionId) != null) {
            monitor.info("Contract definition '%s' already exists".formatted(contractDefinitionId));
            return;
        }

        Criterion criterion = Criterion.Builder.newInstance()
                .operandLeft(EDC_NAMESPACE + "id")
                .operator("=")
                .operandRight(assetId)
                .build();

        ContractDefinition def = ContractDefinition.Builder.newInstance()
                .id(contractDefinitionId)
                .accessPolicyId(policyId)
                .contractPolicyId(policyId)
                .assetsSelectorCriterion(criterion)
                .build();

        contractDefinitionService.create(def)
                .onSuccess(pd -> monitor.info("Contract definition '%s' created".formatted(contractDefinitionId)))
                .onFailure(failure -> {
                    if (failure.getReason() == ServiceFailure.Reason.CONFLICT) {
                        monitor.info("Contract definition already exists: %s".formatted(contractDefinitionId));
                    } else {
                        monitor.warning("Failed to create contract definition: %s".formatted(failure.getReason()));
                    }
                });
    }

    private void validateCreateAsset(CreateAssetRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (request.reportType() == null) {
            throw new IllegalArgumentException("Invalid reportType");
        }
        if (request.method() == null || request.method().isBlank()) {
            throw new IllegalArgumentException("Please provide 'method'");
        }
        if (request.url() == null || request.url().isBlank()) {
            throw new IllegalArgumentException("Please provide asset 'url'");
        }
    }
}
