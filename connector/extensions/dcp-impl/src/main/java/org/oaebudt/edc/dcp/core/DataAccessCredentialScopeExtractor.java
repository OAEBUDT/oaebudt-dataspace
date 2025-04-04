package org.oaebudt.edc.dcp.core;

import org.eclipse.edc.iam.identitytrust.spi.scope.ScopeExtractor;
import org.eclipse.edc.policy.context.request.spi.RequestPolicyContext;
import org.eclipse.edc.policy.model.Operator;

import java.util.Set;

class DataAccessCredentialScopeExtractor implements ScopeExtractor {
    public static final String DATA_PROCESSOR_CREDENTIAL_TYPE = "DataProcessorCredential";
    private static final String DATA_ACCESS_CONSTRAINT_PREFIX = "DataAccess.";
    private static final String CREDENTIAL_TYPE_NAMESPACE = "org.eclipse.edc.vc.type";

    @Override
    public Set<String> extractScopes(Object leftValue, Operator operator, Object rightValue, RequestPolicyContext context) {
        Set<String> scopes = Set.of();
        if (leftValue instanceof String leftOperand) {
            if (leftOperand.startsWith(DATA_ACCESS_CONSTRAINT_PREFIX)) {
                scopes = Set.of("%s:%s:read".formatted(CREDENTIAL_TYPE_NAMESPACE, DATA_PROCESSOR_CREDENTIAL_TYPE));
            }
        }
        return scopes;
    }
}
