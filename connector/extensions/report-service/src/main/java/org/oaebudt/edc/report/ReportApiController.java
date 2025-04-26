package org.oaebudt.edc.report;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.catalog.spi.FederatedCatalogCache;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.oaebudt.edc.report.model.OaebudtPolicyType;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.oaebudt.edc.report.model.Constants.DEFAULT_POLICY_ID;

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
    private final FederatedCatalogCache federatedCatalogCache;


    public ReportApiController(Monitor monitor, ReportStore reportStore, AssetService assetService,
                               ContractDefinitionService contractDefinitionService,
                               FederatedCatalogCache federatedCatalogCache, URI consumerApiBaseUrl) {
        this.reportStore = reportStore;
        this.assetService = assetService;
        this.contractDefinitionService = contractDefinitionService;
        this.monitor = monitor;
        this.consumerApiBaseUrl = consumerApiBaseUrl;
        this.federatedCatalogCache = federatedCatalogCache;
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("title") String title,
                               @FormDataParam("accessLevel") String accessLevel,
                               @FormDataParam("reportType") ReportType reportType) {

        try (JsonReader reader = Json.createReader(uploadedInputStream)) {
            JsonObject jsonObject = reader.readObject();
            OaebudtPolicyType policyType = OaebudtPolicyType.getById(accessLevel);

            JsonObject jsonWithMeta = Json.createObjectBuilder(jsonObject)
                    .add("title", title)
                    .add("reportType", reportType.name())
                    .build();

            reportStore.saveReport(jsonWithMeta.toString(), reportType);

            // create asset if it has not been created already
            String uri = consumerApiBaseUrl.resolve("report?reportType=" + reportType.name()).toString();
            createAssetInConnector(title, reportType, uri, policyType);

            JsonObject body = Json.createObjectBuilder()
                    .add("message", "JSON file uploaded and saved")
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


    @GET
    @Path("/aggregate/{reportType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response aggregateReportBasedOnSearchCriteria(@PathParam("reportType") ReportType reportType,
                                                         @QueryParam("isbn") String isbn,
                                                         @QueryParam("performanceType") String attributePerformance) {
        QuerySpec querySpec = new QuerySpec().toBuilder().build();

        Collection<Catalog> catalogs = federatedCatalogCache.query(querySpec);

        List<Catalog> filteredCatalog = catalogs.stream()
                .filter(catalog -> catalog.getDatasets().getFirst().getId().equals(reportType.name())).toList();

        List<String> result = new ArrayList<>();
        filteredCatalog.forEach(catalog -> {
//            String edr = catalog.getDatasets().get(0).getOffers().
//            result.add(edr);
        });

        return Response.ok().build();
    }



     void createAssetInConnector(String title, ReportType reportType, String uri, OaebudtPolicyType policyType) {

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

        createContractDefinition(reportType.name(), policyType, asset.getId());
    }

    private void createContractDefinition(String contractDefinitionId, OaebudtPolicyType policy, String assetId) {
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
                .accessPolicyId(DEFAULT_POLICY_ID)
                .contractPolicyId(policy.getId())
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

}
