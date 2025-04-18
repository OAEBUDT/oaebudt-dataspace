package org.oaebudt.edc.report;

import io.thinkit.edc.client.connector.EdcConnectorClient;
import io.thinkit.edc.client.connector.model.Asset;
import io.thinkit.edc.client.connector.model.ContractDefinition;
import io.thinkit.edc.client.connector.model.Policy;
import io.thinkit.edc.client.connector.model.PolicyDefinition;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.spi.monitor.Monitor;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

@Path("/")
@Consumes(WILDCARD)
@Produces(WILDCARD)
@MultipartConfig
public class ReportApiController {

    private final ReportStore reportStore;
    private final String managementUrl;
    private final Monitor monitor;


    public ReportApiController(Monitor monitor, ReportStore reportStore, String managementUrl) {
        this.reportStore = reportStore;
        this.managementUrl = managementUrl;
        this.monitor = monitor;
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@Context ContainerRequestContext requestContext, @FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("title") String title,
                               @FormDataParam("reportType") ReportType reportType) {

        try (JsonReader reader = Json.createReader(uploadedInputStream)) {
            JsonObject jsonObject = reader.readObject();

            JsonObject jsonWithMeta = Json.createObjectBuilder(jsonObject)
                    .add("title", title)
                    .add("reportType", reportType.name())
                    .build();

            reportStore.saveReport(jsonWithMeta.toString(), reportType);

            // create asset if it has not been created already
            createAssetInConnector(title, reportType, requestContext.getHeaderString("Authorization"));

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\":\"JSON file uploaded and saved\"}")
                    .build();
        } catch (JsonException e) {
            monitor.warning("Error uploading report", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Failed to process uploaded JSON\"}")
                    .build();
        } catch (Exception e) {
            monitor.warning("Error uploading report", e);
            return Response.serverError()
                    .entity("{\"error\":\"Something went wrong\"}")
                    .build();
        }

    }

     void createAssetInConnector(String title, ReportType reportType, String authorization) {

        var client = EdcConnectorClient.newBuilder()
                .managementUrl(this.managementUrl)
                .interceptor(builder -> builder.header("Authorization", authorization))
                .build();

        createDefaultPolicyDefinition(client, reportType);
        createDefaultContractDefinition(client, reportType);

        Map<String, Object> dataAddress = Map.ofEntries(
                Map.entry("type", "HttpData"),
                Map.entry("name", reportType.name()),
                Map.entry("baseUrl", "title-reporturl"),
                Map.entry("proxyPath", "true")
        );

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

        client.assets().create(asset);

    }

    private void createDefaultPolicyDefinition(EdcConnectorClient client, ReportType reportType) {
        JsonArray emptyArray = Json.createArrayBuilder().build();
        JsonObject policy =  Json.createObjectBuilder()
                .add("@context", "http://www.w3.org/ns/odrl.jsonld")
                .add("@type", "Set")
                .add("permission", emptyArray)
                .add("prohibition", emptyArray)
                .add("obligation", emptyArray)
                .build();

        PolicyDefinition policyDefinition = PolicyDefinition.Builder.newInstance()
                .id(reportType.name())
                .policy(new Policy(policy))
                .build();

        client.policyDefinitions().createAsync(policyDefinition);
    }

    private void createDefaultContractDefinition(EdcConnectorClient client, ReportType reportType) {
        ContractDefinition contractDefinition = ContractDefinition.Builder.newInstance()
                .id(reportType.name())
                .accessPolicyId(reportType.name())
                .contractPolicyId(reportType.name())
                .assetsSelector(Collections.emptyList())
                .build();

        client.contractDefinitions().createAsync(contractDefinition);
    }

}
