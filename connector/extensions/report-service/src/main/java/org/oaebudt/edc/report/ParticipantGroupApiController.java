package org.oaebudt.edc.report;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.policydefinition.PolicyDefinitionService;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.oaebudt.edc.report.dto.AddParticipantToGroupRequest;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;

@Path("/group")
@Consumes(WILDCARD)
@Produces(WILDCARD)
public class ParticipantGroupApiController {

    private static final String TRUSTED_GROUP_POLICY_PREFIX = "allow-";

    private final ParticipantGroupStore participantGroupStore;
    private final PolicyDefinitionService policyDefinitionService;
    private final Monitor monitor;


    public ParticipantGroupApiController(ParticipantGroupStore participantGroupStore,
                                         PolicyDefinitionService policyDefinitionService,
                                         Monitor monitor) {
        this.participantGroupStore = participantGroupStore;
        this.policyDefinitionService = policyDefinitionService;
        this.monitor = monitor;
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addParticipantToGroup(AddParticipantToGroupRequest request) {
        try {
            if(Objects.isNull(request.getGroupName())) {
                throw new IllegalArgumentException("Invalid group name");
            }

            participantGroupStore.putParticipantsInGroup(request.getGroupName().toLowerCase(),
                    request.getParticipants().toArray(new String[0]));

            createTrustedGroupPolicyDefinition(request.getGroupName().toLowerCase());

            JsonObject body = Json.createObjectBuilder()
                    .add("message", "Participants added to group")
                    .build();

            return Response.status(Response.Status.CREATED)
                    .entity(body.toString())
                    .build();
        } catch (IllegalArgumentException e) {
            monitor.warning("Error creating participant group", e);
            JsonObject body = Json.createObjectBuilder()
                    .add("error", "Something went wrong")
                    .add("message", e.getMessage())
                    .build();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(body.toString())
                    .build();
        }
    }

    @Path("/")
    @GET
    public Response getParticipantGroup() {

        Map<String, Set<String>> participantGroups = participantGroupStore.getAllGroups();

        return Response.status(Response.Status.CREATED)
                .entity(participantGroups)
                .build();
    }


    private void createTrustedGroupPolicyDefinition(String trustedGroup) {

        Policy policy = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .action(Action.Builder.newInstance()
                                .type(ODRL_SCHEMA + "use")
                                .build())
                        .constraint(AtomicConstraint.Builder.newInstance()
                                .leftExpression(new LiteralExpression("TrustedGroup"))
                                .operator(Operator.EQ)
                                .rightExpression(new LiteralExpression(trustedGroup))
                                .build())
                        .build())
                .type(PolicyType.SET)
                .build();

        PolicyDefinition policyDefinition = PolicyDefinition.Builder.newInstance()
                .policy(policy)
                .id(TRUSTED_GROUP_POLICY_PREFIX + trustedGroup)
                .build();

        policyDefinitionService.create(policyDefinition)
                .onSuccess(pd -> monitor.info("Default policy definition '%s' created successfully"
                        .formatted(TRUSTED_GROUP_POLICY_PREFIX + trustedGroup)))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Default policy definition '%s' already exists"
                                .formatted(TRUSTED_GROUP_POLICY_PREFIX + trustedGroup));
                    } else {
                        monitor.warning("Unable to create policy definition '%s'. Reason: %s"
                                .formatted(TRUSTED_GROUP_POLICY_PREFIX + trustedGroup, serviceFailure.getReason()));
                    }
                });
    }

}
