package org.oaebudt.edc.report;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.connector.dataplane.http.spi.HttpDataAddress;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.oaebudt.edc.report.dto.CreateAssetRequest;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

@Path("/")
@Consumes(WILDCARD)
@Produces(WILDCARD)
@MultipartConfig
public class ReportApiController {

    private final ReportStore reportStore;
    private final Monitor monitor;
    private final AssetService assetService;
    private final ContractDefinitionService contractDefinitionService;
    private final URI consumerApiBaseUrl;


    public ReportApiController(Monitor monitor, ReportStore reportStore, AssetService assetService,
                               ContractDefinitionService contractDefinitionService, URI consumerApiBaseUrl) {
        this.reportStore = reportStore;
        this.assetService = assetService;
        this.contractDefinitionService = contractDefinitionService;
        this.monitor = monitor;
        this.consumerApiBaseUrl = consumerApiBaseUrl;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAsset(CreateAssetRequest request) {
        try {
            validateCreateAsset(request);

            createAssetInConnector(request.getTitle(), request.getMetadata(), request.getReportType(),
                    request.getAccessDefinition(), request.getUrl(), request.getMethod(), request.getAuthHeaderKey(),
                    request.getAuthCode(), request.getHeaders());


            JsonObject body = Json.createObjectBuilder()
                    .add("message", "Asset created successfully")
                    .build();

            return Response.status(Response.Status.CREATED)
                    .entity(body.toString())
                    .build();

        } catch (IllegalArgumentException e) {
            monitor.warning("Error uploading report", e);
            JsonObject body = Json.createObjectBuilder()
                    .add("error", "Something went wrong")
                    .add("message", e.getMessage())
                    .build();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(body.toString())
                    .build();
        }
    }


    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("title") String title,
                               @FormDataParam("accessDefinition") String accessDefinition,
                               @FormDataParam("reportType") ReportType reportType) {

        try (JsonReader reader = Json.createReader(uploadedInputStream)) {
            JsonObject jsonObject = reader.readObject();

            JsonObject jsonWithMeta = Json.createObjectBuilder(jsonObject)
                    .add("title", title)
                    .add("reportType", reportType.name())
                    .build();

            reportStore.saveReport(jsonWithMeta.toString(), reportType);

            // create asset if it has not been created already
            String uri = consumerApiBaseUrl.resolve("report?reportType=" + reportType.name()).toString();
            createAssetInConnector(title, reportType, uri, accessDefinition);

            JsonObject body = Json.createObjectBuilder()
                    .add("message", "Asset created successfully")
                    .build();

            return Response.status(Response.Status.CREATED)
                    .entity(body.toString())
                    .build();
        } catch (JsonException e) {
            monitor.warning("Error uploading report", e);
            JsonObject body = Json.createObjectBuilder()
                    .add("error", "Failed to process uploaded JSON")
                    .build();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(body.toString())
                    .build();
        } catch (IllegalArgumentException e) {
            monitor.warning("Error uploading report", e);
            JsonObject body = Json.createObjectBuilder()
                    .add("error", "Something went wrong")
                    .add("message", e.getMessage())
                    .build();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(body.toString())
                    .build();
        } catch (Exception e) {
            monitor.warning("Error uploading report", e);
            JsonObject body = Json.createObjectBuilder()
                    .add("error", "Something went wrong")
                    .add("message", e.getMessage())
                    .build();
            return Response.serverError()
                    .entity(body.toString())
                    .build();
        }

    }

    void createAssetInConnector(String title, String assetMetadata, ReportType reportType, String accessDefinition, String uri,
                                String method, String authKey, String authCode, Map<String, Object> headers) {

        DataAddress dataAddress = HttpDataAddress.Builder.newInstance()
                .type("HttpData")
                .baseUrl(uri)
                .authKey(authKey)
                .authCode(authCode)
                .method(method)
                .properties(headers)
                .build();

        Map<String, Object> properties = Map.ofEntries(
                Map.entry("name", title),
                Map.entry("contentType", "application/json"),
                Map.entry("type", reportType.name()),
                Map.entry("metadata", assetMetadata)
        );

        Asset asset =  Asset.Builder.newInstance()
                .id(reportType.name())
                .properties(properties)
                .dataAddress(dataAddress)
                .build();

        assetService.create(asset)
                .onSuccess(pd -> monitor.info("Asset '%s' created successfully".formatted(reportType.name())))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Asset '%s' already exists".formatted(reportType.name()));
                    } else {
                        monitor.warning("Unable to create asset '%s'. Reason: %s".formatted(
                                reportType.name(), serviceFailure.getReason()));
                    }
                });


        String contractDefinitionId = "%s-%s".formatted(accessDefinition, reportType.name());
        createContractDefinition(contractDefinitionId, accessDefinition, asset.getId());

    }

    void createAssetInConnector(String title, ReportType reportType, String uri, String accessDefinition) {

        DataAddress dataAddress = DataAddress.Builder.newInstance()
                .type("HttpData")
                .property("name", reportType.name())
                .property(EDC_NAMESPACE + "baseUrl", uri)
                .property("proxyPath", "true")
                .build();

        Map<String, Object> properties = Map.ofEntries(
                Map.entry("name", title),
                Map.entry("contentType", "application/json"),
                Map.entry("type", reportType.name())
        );

        Asset asset =  Asset.Builder.newInstance()
                .id(reportType.name())
                .properties(properties)
                .dataAddress(dataAddress)
                .build();

        assetService.create(asset)
                .onSuccess(pd -> monitor.info("Asset '%s' created successfully".formatted(reportType.name())))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Asset '%s' already exists".formatted(reportType.name()));
                    } else {
                        monitor.warning("Unable to create asset '%s'. Reason: %s".formatted(
                                reportType.name(), serviceFailure.getReason()));
                    }
                });


        String contractDefinitionId = "%s-%s".formatted(accessDefinition, reportType.name());
        createContractDefinition(contractDefinitionId, accessDefinition, asset.getId());
    }

    private void createContractDefinition(String contractDefinitionId, String policyId, String assetId) {
        if(Objects.nonNull(contractDefinitionService.findById(contractDefinitionId))) {
            monitor.info("Contract definition '%s' already exists".formatted(contractDefinitionId));
            return;
        }

        Criterion criterion = Criterion.Builder.newInstance()
                .operandLeft(EDC_NAMESPACE + "id")
                .operator("=")
                .operandRight(assetId)
                .build();
        ContractDefinition contractDefinition = ContractDefinition.Builder.newInstance()
                .id(contractDefinitionId)
                .accessPolicyId(policyId)
                .contractPolicyId(policyId)
                .assetsSelectorCriterion(criterion)
                .build();

        contractDefinitionService.create(contractDefinition)
                .onSuccess(pd -> monitor.info("Contract definition '%s' created successfully".formatted(contractDefinitionId)))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Contract definition '%s' already exists".formatted(contractDefinitionId));
                    } else {
                        monitor.warning("Unable to create contract definition '%s'. Reason: %s".formatted(
                                contractDefinitionId, serviceFailure.getReason()));
                    }
                });
    }

    private void validateCreateAsset(CreateAssetRequest request) {
        if(request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        if(Objects.isNull(request.getReportType())) {
            throw new IllegalArgumentException("Invalid reportType");
        }
        if(request.getMethod() == null || request.getMethod().isBlank()) {
            throw new IllegalArgumentException("Please provide 'method'");
        }

        if(request.getUrl() == null || request.getUrl().isBlank()) {
            throw new IllegalArgumentException("Please provide asset 'url'");
        }
    }

}
