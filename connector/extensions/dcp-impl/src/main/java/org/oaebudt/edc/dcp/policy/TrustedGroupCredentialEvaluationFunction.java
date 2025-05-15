package org.oaebudt.edc.dcp.policy;

import org.eclipse.edc.participant.spi.ParticipantAgentPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Objects;

public record TrustedGroupCredentialEvaluationFunction<C extends ParticipantAgentPolicyContext>(
        ParticipantGroupStore participantGroupStore) implements AtomicConstraintRuleFunction<Permission, C> {

    public static final String TRUSTED_GROUP_CONSTRAINT_KEY = "TrustedGroup";

    public static <C extends ParticipantAgentPolicyContext> TrustedGroupCredentialEvaluationFunction<C> create(ParticipantGroupStore participantGroupStore) {
        return new TrustedGroupCredentialEvaluationFunction<>(participantGroupStore);
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, C policyContext) {

        if (!operator.equals(Operator.EQ)) {
            policyContext.reportProblem("Invalid operator '%s', only accepts '%s'".formatted(operator, Operator.EQ));
            return false;
        }

        var participantAgent = policyContext.participantAgent();

        ParticipantGroup participantGroup = participantGroupStore.findById(rightValue.toString());
        if (Objects.isNull(participantGroup)) {
            policyContext.reportProblem("Participant group doesn't exist");
            return false;
        }

        return participantGroup.contains(participantAgent.getIdentity());
    }
}
