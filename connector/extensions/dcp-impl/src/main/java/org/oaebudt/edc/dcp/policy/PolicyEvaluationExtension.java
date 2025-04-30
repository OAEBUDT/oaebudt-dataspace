package org.oaebudt.edc.dcp.policy;

import org.eclipse.edc.connector.controlplane.catalog.spi.policy.CatalogPolicyContext;
import org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext;
import org.eclipse.edc.connector.controlplane.contract.spi.policy.TransferProcessPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import static org.oaebudt.edc.dcp.policy.MembershipCredentialEvaluationFunction.MEMBERSHIP_CONSTRAINT_KEY;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;
import static org.oaebudt.edc.dcp.policy.TrustedGroupCredentialEvaluationFunction.TRUSTED_GROUP_CONSTRAINT_KEY;

public class PolicyEvaluationExtension implements ServiceExtension {

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private ParticipantGroupStore participantGroupStore;

    CredentialExtractor credentialExtractor = new CredentialExtractor();

    @Override
    public void initialize(ServiceExtensionContext context) {

        bindPermissionFunction(MembershipCredentialEvaluationFunction.create(credentialExtractor), TransferProcessPolicyContext.class, TransferProcessPolicyContext.TRANSFER_SCOPE, MEMBERSHIP_CONSTRAINT_KEY);
        bindPermissionFunction(MembershipCredentialEvaluationFunction.create(credentialExtractor), ContractNegotiationPolicyContext.class, ContractNegotiationPolicyContext.NEGOTIATION_SCOPE, MEMBERSHIP_CONSTRAINT_KEY);
        bindPermissionFunction(MembershipCredentialEvaluationFunction.create(credentialExtractor), CatalogPolicyContext.class, CatalogPolicyContext.CATALOG_SCOPE, MEMBERSHIP_CONSTRAINT_KEY);

        bindPermissionFunction(TrustedGroupCredentialEvaluationFunction.create(participantGroupStore), TransferProcessPolicyContext.class, TransferProcessPolicyContext.TRANSFER_SCOPE, TRUSTED_GROUP_CONSTRAINT_KEY);
        bindPermissionFunction(TrustedGroupCredentialEvaluationFunction.create(participantGroupStore), ContractNegotiationPolicyContext.class, ContractNegotiationPolicyContext.NEGOTIATION_SCOPE, TRUSTED_GROUP_CONSTRAINT_KEY);
        bindPermissionFunction(TrustedGroupCredentialEvaluationFunction.create(participantGroupStore), CatalogPolicyContext.class, CatalogPolicyContext.CATALOG_SCOPE, TRUSTED_GROUP_CONSTRAINT_KEY);

        registerDataAccessLevelFunction();

    }

    private void registerDataAccessLevelFunction() {
        var accessLevelKey = "DataAccess.level";

        bindDutyFunction(DataAccessLevelFunction.create(credentialExtractor), TransferProcessPolicyContext.class, TransferProcessPolicyContext.TRANSFER_SCOPE, accessLevelKey);
        bindDutyFunction(DataAccessLevelFunction.create(credentialExtractor), ContractNegotiationPolicyContext.class, ContractNegotiationPolicyContext.NEGOTIATION_SCOPE, accessLevelKey);
        bindDutyFunction(DataAccessLevelFunction.create(credentialExtractor), CatalogPolicyContext.class, CatalogPolicyContext.CATALOG_SCOPE, accessLevelKey);
    }

    private <C extends PolicyContext> void bindPermissionFunction(AtomicConstraintRuleFunction<Permission, C> function, Class<C> contextClass, String scope, String constraintType) {
        ruleBindingRegistry.bind("use", scope);
        ruleBindingRegistry.bind(ODRL_SCHEMA + "use", scope);
        ruleBindingRegistry.bind(constraintType, scope);

        policyEngine.registerFunction(contextClass, Permission.class, constraintType, function);
    }

    private <C extends PolicyContext> void bindDutyFunction(AtomicConstraintRuleFunction<Duty, C> function, Class<C> contextClass, String scope, String constraintType) {
        ruleBindingRegistry.bind("use", scope);
        ruleBindingRegistry.bind(ODRL_SCHEMA + "use", scope);
        ruleBindingRegistry.bind(constraintType, scope);

        policyEngine.registerFunction(contextClass, Duty.class, constraintType, function);
    }
}
