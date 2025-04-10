package org.oaebudt.edc.report;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

@Path("/report")
@Consumes(WILDCARD)
@Produces(WILDCARD)
public class ReportApiController {

    @GET
    public Response proxyGet(@Context ContainerRequestContext requestContext) {
        return Response.ok("Hello").build();
    }
}
