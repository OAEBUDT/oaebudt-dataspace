package org.oaebudt.edc.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.spi.monitor.Monitor;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.oaebudt.edc.web.dto.CreateAssetRequest;
import org.oaebudt.edc.web.model.ReportType;
import org.oaebudt.edc.web.service.ReportService;

import java.io.InputStream;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

@Path("/report")
@Consumes(WILDCARD)
@Produces(WILDCARD)
@MultipartConfig
public class ReportApiController {

    private final ReportService reportService;
    private final Monitor monitor;

    public ReportApiController(Monitor monitor, ReportService reportService) {
        this.monitor = monitor;
        this.reportService = reportService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAsset(CreateAssetRequest request) {
        try {
            reportService.createAsset(request);
            return Response.status(Response.Status.CREATED)
                    .entity(Json.createObjectBuilder().add("message", "Asset created successfully").build().toString())
                    .build();
        } catch (IllegalArgumentException e) {
            return badRequest("Something went wrong", e.getMessage());
        }
    }


    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("title") String title,
                               @FormDataParam("accessDefinition") String accessDefinition,
                               @FormDataParam("metadata") String metadataJson,
                               @FormDataParam("reportType") ReportType reportType) {

        try {
            reportService.uploadAndCreateAsset(uploadedInputStream, title, accessDefinition, metadataJson, reportType);
            return Response.status(Response.Status.CREATED)
                    .entity(Json.createObjectBuilder().add("message", "Asset created successfully").build().toString())
                    .build();
        } catch (JsonException e) {
            return badRequest("Failed to process uploaded JSON", e.getMessage());
        } catch (JsonProcessingException e) {
            return badRequest("Invalid JSON metadata", e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("Something went wrong", e.getMessage());
        } catch (Exception e) {
            monitor.warning("Unexpected error", e);
            return Response.serverError()
                    .entity(Json.createObjectBuilder().add("error", "Unexpected error").add("message", e.getMessage()).build().toString())
                    .build();
        }

    }

    private Response badRequest(String error, String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Json.createObjectBuilder().add("error", error).add("message", message).build().toString())
                .build();
    }

}
