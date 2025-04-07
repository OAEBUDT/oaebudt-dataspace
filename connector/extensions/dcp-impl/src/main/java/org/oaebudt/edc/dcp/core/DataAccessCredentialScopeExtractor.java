package org.oaebudt.edc.dcp.core;

import org.eclipse.edc.iam.identitytrust.spi.scope.ScopeExtractor;
import org.eclipse.edc.policy.context.request.spi.RequestPolicyContext;
import org.eclipse.edc.policy.model.Operator;

import java.util.Collections;
import java.util.Set;

class DataAccessCredentialScopeExtractor implements ScopeExtractor {
    public static final String DATA_PROCESSOR_CREDENTIAL_TYPE = "DataProcessorCredential";
    private static final String DATA_ACCESS_CONSTRAINT_PREFIX = "DataAccess.";
    private static final String CREDENTIAL_TYPE_NAMESPACE = "org.eclipse.edc.vc.type";

    @Override
    public Set<String> extractScopes(Object leftValue, Operator operator, Object rightValue, RequestPolicyContext context) {
        if (leftValue instanceof String leftOperand && leftOperand.startsWith(DATA_ACCESS_CONSTRAINT_PREFIX)) {
            return Set.of("%s:%s:read".formatted(CREDENTIAL_TYPE_NAMESPACE, DATA_PROCESSOR_CREDENTIAL_TYPE));
        }
        return Collections.emptySet();
    }
}
