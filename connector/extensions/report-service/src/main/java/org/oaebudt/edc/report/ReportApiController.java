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
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;

import java.io.InputStream;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

@Path("/")
@Consumes(WILDCARD)
@Produces(WILDCARD)
@MultipartConfig
public class ReportApiController {

    private final ReportStore reportStore;
    private final Monitor monitor;
    private final AssetService assetService;


    public ReportApiController(Monitor monitor, ReportStore reportStore, AssetService assetService) {
        this.reportStore = reportStore;
        this.assetService = assetService;
        this.monitor = monitor;
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
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
            createAssetInConnector(title, reportType);

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

     void createAssetInConnector(String title, ReportType reportType) {

        DataAddress dataAddress = DataAddress.Builder.newInstance()
                .type("HttpData")
                .property("name", reportType.name())
                .property("baseUrl", "title-reporturl")
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
                });;
    }

}
