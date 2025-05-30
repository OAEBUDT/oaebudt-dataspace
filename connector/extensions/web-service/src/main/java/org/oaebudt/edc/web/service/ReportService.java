package org.oaebudt.edc.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonException;
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
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.oaebudt.edc.web.dto.CreateAssetRequest;
import org.oaebudt.edc.web.model.ReportType;
import org.oaebudt.edc.web.repository.ReportStore;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public ServiceResult<String> createAsset(CreateAssetRequest request) {
        ValidationResult validationResult = validateCreateAsset(request);
        validationResult.merge(MetadataValidator.validateMetadata(request.metadata()));

        if(validationResult.failed()) {
            return ServiceResult.badRequest(validationResult.getErrors());
        }

        String assetId = getUniqueAssetId(request.reportType());
        createAssetInConnector(
                assetId,
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
        return ServiceResult.success(assetId);
    }

    private String getUniqueAssetId(ReportType reportType) {
        return "%s-%s".formatted(reportType, UUID.randomUUID());
    }

    public ServiceResult<String> uploadAndCreateAsset(InputStream file, String title, String accessDefinition, String metadataJson, ReportType reportType) {
        try (JsonReader reader = Json.createReader(file)) {
            if (Objects.isNull(accessDefinition)) {
                return ServiceResult.badRequest("Invalid access definition");
            }

            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {});

            ValidationResult validationResult = MetadataValidator.validateMetadata(metadata);
            if(validationResult.failed()) {
                return ServiceResult.badRequest(validationResult.getErrors());
            }

            JsonObject jsonObject = reader.readObject();
            JsonObject enriched = Json.createObjectBuilder(jsonObject)
                    .add("title", title)
                    .add("reportType", reportType.name())
                    .build();

            reportStore.saveReport(enriched.toString(), reportType);

            String uri = consumerApiBaseUrl.resolve("report?reportType=" + reportType.name()).toString();
            String assetId = getUniqueAssetId(reportType);
            createAssetInConnector(assetId, title, metadata, reportType, accessDefinition, uri, "GET", null, null, Collections.emptyMap());

            return ServiceResult.success(assetId);
        } catch (JsonProcessingException e) {
            return ServiceResult.badRequest("Invalid metadata json: " + e.getMessage());
        } catch (JsonException e) {
            return ServiceResult.badRequest("Invalid report json: " + e.getMessage());
        }

    }

    private void createAssetInConnector(String assetId, String title, Map<String, Object> assetMetadata, ReportType reportType,
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

        Asset asset = Asset.Builder.newInstance()
                .id(assetId)
                .properties(Map.of(
                        EDC_NAMESPACE + "name", title,
                        EDC_NAMESPACE + "type", reportType.name()
                ))
                .properties(correctPropertiesContext(assetMetadata))
                .dataAddress(dataAddress)
                .build();

        assetService.create(asset)
                .onSuccess(pd -> monitor.info("Asset '%s' created".formatted(assetId)))
                .onFailure(failure -> {
                    if (failure.getReason() == ServiceFailure.Reason.CONFLICT) {
                        monitor.info("Asset already exists: %s".formatted(assetId));
                    } else {
                        monitor.warning("Failed to create asset: %s".formatted(failure.getReason()));
                    }
                });

        createContractDefinition(accessDefinition + "-" + assetId, accessDefinition, assetId);
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

    private ValidationResult validateCreateAsset(CreateAssetRequest request) {
        ValidationResult result = ValidationResult.ok();

        List<ValidationResult> validationResults = new ArrayList<>();

        validationResults.add(validateRequiredString(request.title(), "title"));
        validationResults.add(validateRequiredString(request.method(), "method"));
        validationResults.add(validateRequiredString(request.url(), "url"));
        if (request.reportType() == null) {
            validationResults.add(ValidationResult.fail("Invalid Report type"));
        }

        return validationResults.stream().reduce(result, (a, b)-> {
            a.merge(b);
            return a;
        });
    }

    private ValidationResult validateRequiredString(String value, String key) {
        if (value == null || value.isBlank()) {
            return ValidationResult.fail(key + " is required and must be a non-empty string");
        }
        return ValidationResult.ok();
    }


    private Map<String, Object> correctPropertiesContext(Map<String, Object> properties) {

        return properties.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().startsWith(EDC_NAMESPACE) ? entry.getKey() : "%s%s".formatted(EDC_NAMESPACE, entry.getKey()),
                        Map.Entry::getValue
                ));
    }
}
