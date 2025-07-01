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
