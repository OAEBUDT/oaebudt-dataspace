package org.oaebudt.edc.dcp.policy;

import org.eclipse.edc.participant.spi.ParticipantAgentPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Objects;
import java.util.Set;

public class TrustedGroupCredentialEvaluationFunction<C extends ParticipantAgentPolicyContext> implements AtomicConstraintRuleFunction<Permission, C> {

    public static final String TRUSTED_GROUP_CONSTRAINT_KEY = "TrustedGroup";
    private final ParticipantGroupStore participantGroupStore;

    public static <C extends ParticipantAgentPolicyContext> TrustedGroupCredentialEvaluationFunction<C> create(ParticipantGroupStore participantGroupStore) {
        return new TrustedGroupCredentialEvaluationFunction<>(participantGroupStore);
    }

    public TrustedGroupCredentialEvaluationFunction(ParticipantGroupStore participantGroupStore) {
        this.participantGroupStore = participantGroupStore;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, C policyContext) {

        if (!operator.equals(Operator.EQ)) {
            policyContext.reportProblem("Invalid operator '%s', only accepts '%s'".formatted(operator, Operator.EQ));
            return false;
        }

        var pa = policyContext.participantAgent();
        if (pa == null) {
            policyContext.reportProblem("No ParticipantAgent found on context.");
            return false;
        }

        Set<String> trustedParticipants = participantGroupStore.getParticipantsByGroupId(rightValue.toString());
        if(Objects.isNull(trustedParticipants)) {
            return false;
        }

        return trustedParticipants.contains(pa.getIdentity());
    }
}
