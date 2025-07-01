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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.spi.monitor.Monitor;
import org.oaebudt.edc.web.dto.AddParticipantToGroupRequest;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.web.service.ParticipantGroupService;

import java.util.Set;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

@Path("/participant/group")
@Consumes(WILDCARD)
@Produces(WILDCARD)
public class ParticipantGroupApiController {

    private final ParticipantGroupService participantGroupService;
    private final Monitor monitor;

    public ParticipantGroupApiController(ParticipantGroupService participantGroupService, Monitor monitor) {
        this.participantGroupService = participantGroupService;
        this.monitor = monitor;
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addParticipantToGroup(AddParticipantToGroupRequest request) {
        try {
            participantGroupService.addParticipantsToGroup(request);

            JsonObject body = Json.createObjectBuilder()
                    .add("message", "Participants added to group")
                    .build();

            return Response.status(Response.Status.CREATED).entity(body.toString()).build();

        } catch (IllegalArgumentException e) {
            monitor.warning("Error creating participant group", e);
            JsonObject body = Json.createObjectBuilder()
                    .add("error", "Something went wrong")
                    .add("message", e.getMessage())
                    .build();
            return Response.status(Response.Status.BAD_REQUEST).entity(body.toString()).build();
        }
    }

    @Path("/")
    @GET
    public Response getParticipantGroup() {
        Set<ParticipantGroup> participantGroups = participantGroupService.getAllGroups();
        return Response.status(Response.Status.OK).entity(participantGroups).build();
    }
}
