package org.oaebudt.edc.web;

import jakarta.json.Json;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    public ReportApiController(ReportService reportService) {
        this.reportService = reportService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAsset(CreateAssetRequest request) {

        return reportService.createAsset(request)
                .map(assetId -> Response.status(Response.Status.CREATED)
                        .entity(Json.createObjectBuilder().add("message", "Asset created successfully").add("assetId", assetId).build().toString())
                        .build()).orElse(serviceFailure -> badRequest("Something went wrong", serviceFailure.getFailureDetail()));
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

        return reportService.uploadAndCreateAsset(uploadedInputStream, title, accessDefinition, metadataJson, reportType)
                .map(assetId -> Response.status(Response.Status.CREATED)
                        .entity(Json.createObjectBuilder().add("message", "Asset created successfully").add("assetId", assetId).build().toString())
                        .build()).orElse(serviceFailure -> badRequest("Something went wrong", serviceFailure.getFailureDetail()));
    }

    private Response badRequest(String error, String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Json.createObjectBuilder().add("error", error).add("message", message).build().toString())
                .build();
    }

}
