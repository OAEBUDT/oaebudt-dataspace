/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oaebudt.edc.web;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.oaebudt.edc.web.model.ReportType;
import org.oaebudt.edc.web.repository.ReportStore;

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
