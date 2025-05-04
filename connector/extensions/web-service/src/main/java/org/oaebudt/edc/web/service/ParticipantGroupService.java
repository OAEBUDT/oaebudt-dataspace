package org.oaebudt.edc.web.service;

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
import org.oaebudt.edc.web.dto.AddParticipantToGroupRequest;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Objects;
import java.util.Set;

import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;

public class ParticipantGroupService {

    private static final String TRUSTED_GROUP_POLICY_PREFIX = "allow-";

    private final ParticipantGroupStore participantGroupStore;
    private final PolicyDefinitionService policyDefinitionService;
    private final Monitor monitor;

    public ParticipantGroupService(ParticipantGroupStore participantGroupStore,
                                   PolicyDefinitionService policyDefinitionService,
                                   Monitor monitor) {
        this.participantGroupStore = participantGroupStore;
        this.policyDefinitionService = policyDefinitionService;
        this.monitor = monitor;
    }

    public void addParticipantsToGroup(AddParticipantToGroupRequest request) {
        if (Objects.isNull(request.groupName())) {
            throw new IllegalArgumentException("Invalid group name");
        }

        String groupName = request.groupName().toLowerCase();
        participantGroupStore.save(groupName, request.participants().toArray(new String[0]));
        createTrustedGroupPolicyDefinition(groupName);
    }

    public Set<ParticipantGroup> getAllGroups() {
        return participantGroupStore.getAllGroups();
    }

    private void createTrustedGroupPolicyDefinition(String trustedGroup) {
        Policy policy = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .action(Action.Builder.newInstance().type(ODRL_SCHEMA + "use").build())
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
                    if (serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Default policy definition '%s' already exists"
                                .formatted(TRUSTED_GROUP_POLICY_PREFIX + trustedGroup));
                    } else {
                        monitor.warning("Unable to create policy definition '%s'. Reason: %s"
                                .formatted(TRUSTED_GROUP_POLICY_PREFIX + trustedGroup, serviceFailure.getReason()));
                    }
                });
    }
}
