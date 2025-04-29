package org.oaebudt.edc.report;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

@Path("/")
@Consumes(WILDCARD)
@Produces(WILDCARD)
public class ConsumerApiController {
    private final ReportStore reportStore;

    public ConsumerApiController(ReportStore reportStore) {
        this.reportStore = reportStore;
    }

    @GET
    @Path("report")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportWithSearchCriteria(@QueryParam("reportType") ReportType reportType) {
        Document document = reportStore.getReportByType(reportType);
        return Response.status(Response.Status.CREATED)
                .entity(document)
                .build();
    }
}
