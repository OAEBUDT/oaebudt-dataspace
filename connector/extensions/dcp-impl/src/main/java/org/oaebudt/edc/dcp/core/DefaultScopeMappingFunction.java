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

package org.oaebudt.edc.dcp.core;

import org.eclipse.edc.policy.context.request.spi.RequestPolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyValidatorRule;
import org.eclipse.edc.policy.model.Policy;

import java.util.HashSet;
import java.util.Set;

public class DefaultScopeMappingFunction implements PolicyValidatorRule<RequestPolicyContext> {
    private final Set<String> defaultScopes;

    public DefaultScopeMappingFunction(Set<String> defaultScopes) {
        this.defaultScopes = defaultScopes;
    }

    @Override
    public Boolean apply(Policy policy, RequestPolicyContext requestPolicyContext) {
        var requestScopeBuilder = requestPolicyContext.requestScopeBuilder();
        var rq = requestScopeBuilder.build();
        var existingScope = rq.getScopes();
        var newScopes = new HashSet<>(defaultScopes);
        newScopes.addAll(existingScope);
        requestScopeBuilder.scopes(newScopes);
        return true;
    }
}
