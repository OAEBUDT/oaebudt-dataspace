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
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Operator;

import java.util.List;
import java.util.Objects;

public class DataAccessLevelFunction<C extends ParticipantAgentPolicyContext> implements AtomicConstraintRuleFunction<Duty, C> {

    private static final String DATAPROCESSOR_CRED_TYPE = "DataProcessorCredential";
    private static final String MVD_NAMESPACE = "https://w3id.org/mvd/credentials/";

    private final CredentialExtractor credentialExtractor;

    // Constructor to inject the CredentialExtractor
    public DataAccessLevelFunction(CredentialExtractor credentialExtractor) {
        this.credentialExtractor = credentialExtractor;
    }

    public static <C extends ParticipantAgentPolicyContext> DataAccessLevelFunction<C> create(CredentialExtractor credentialExtractor) {
        return new DataAccessLevelFunction<>(credentialExtractor);
    }

    @Override
    public boolean evaluate(Operator operator, Object rightOperand, Duty duty, C policyContext) {
        if (!operator.equals(Operator.EQ)) {
            policyContext.reportProblem("Cannot evaluate operator %s, only %s is supported".formatted(operator, Operator.EQ));
            return false;
        }

        var pa = policyContext.participantAgent();
        if (pa == null) {
            policyContext.reportProblem("ParticipantAgent not found on PolicyContext");
            return false;
        }

        var credentialResult = credentialExtractor.extractCredentials(pa);
        if (credentialResult.failed()) {
            policyContext.reportProblem(credentialResult.getFailureDetail());
            return false;
        }

        return credentialResult.getContent()
                .stream()
                .filter(vc -> vc.getType().stream().anyMatch(t -> t.endsWith(DATAPROCESSOR_CRED_TYPE)))
                .flatMap(credential -> credential.getCredentialSubject().stream())
                .anyMatch(credentialSubject -> {
                    var version = credentialSubject.getClaim(MVD_NAMESPACE, "contractVersion");
                    var level = credentialSubject.getClaim(MVD_NAMESPACE, "level");

                    return version != null && Objects.equals(level, rightOperand);
                });
    }
}
