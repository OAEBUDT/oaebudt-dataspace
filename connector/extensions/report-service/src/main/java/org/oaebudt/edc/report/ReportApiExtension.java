package org.oaebudt.edc.report;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.connector.controlplane.services.spi.policydefinition.PolicyDefinitionService;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;
import org.oaebudt.edc.report.model.OaebudtPolicyType;
import org.oaebudt.edc.report.repository.ReportStore;
import org.oaebudt.edc.report.repository.MongoReportStoreImpl;


import java.net.URI;
import java.util.Collections;

import static java.lang.String.format;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;
import static org.oaebudt.edc.report.ReportApiExtension.NAME;
import static org.oaebudt.edc.report.model.Constants.DEFAULT_POLICY_ID;

@Extension(NAME)
public class ReportApiExtension implements ServiceExtension {
    public static final String NAME = "Report API";
    private static final String REPORT = "report";
    private static final String CONSUMER = "consumer";

    @Configuration
    private ReportApiConfiguration reportApiConfiguration;

    @Configuration
    private ConsumerApiConfiguration consumerApiConfiguration;

    @Configuration
    private MongoDbConfiguration mongoDbConfiguration;

    @Inject
    private WebService webService;

    @Inject
    private PortMappingRegistry portMappingRegistry;

    @Inject
    private AssetService assetService;

    @Inject
    private PolicyDefinitionService policyDefinitionService;

    @Inject
    private ContractDefinitionService contractDefinitionService;

    @Inject
    private Hostname hostname;

    private Monitor monitor;

    @Override
    public void initialize(final ServiceExtensionContext context) {
        this.monitor = context.getMonitor();

        URI consumerApiBaseUrl = URI.create(format("http://%s:%s%s/", hostname.get(), consumerApiConfiguration.port(), consumerApiConfiguration.path()));

        MongoClient client = MongoClients.create(mongoDbConfiguration.datasourceUrl());
        ReportStore reportStore = new MongoReportStoreImpl(client);

        portMappingRegistry.register(new PortMapping(REPORT, reportApiConfiguration.port(), reportApiConfiguration.path()));
        portMappingRegistry.register(new PortMapping(CONSUMER, consumerApiConfiguration.port(), consumerApiConfiguration.path()));

        webService.registerResource(REPORT, new ReportApiController(
                context.getMonitor(),
                reportStore, assetService, contractDefinitionService, consumerApiBaseUrl));
        webService.registerResource(CONSUMER, new ConsumerApiController(reportStore));

    }

    @Override
    public void start() {
        // seed default policies and contract definitions for all report types
        // Membership policy is the default access Policy
        createDefaultPolicyDefinition();
        // create other contract policies
        for(OaebudtPolicyType type: OaebudtPolicyType.values()) {
            createPolicyDefinition(type);
        }
    }

    private void createDefaultPolicyDefinition() {

        Policy policy = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .action(Action.Builder.newInstance()
                                .type(ODRL_SCHEMA + "use")
                                .build())
                        .constraint(AtomicConstraint.Builder.newInstance()
                                .leftExpression(new LiteralExpression("MembershipCredential"))
                                .operator(Operator.EQ)
                                .rightExpression(new LiteralExpression("active"))
                                .build())
                        .build())
                .type(PolicyType.SET)
                .build();

        PolicyDefinition policyDefinition = PolicyDefinition.Builder.newInstance()
                .policy(policy)
                .id(DEFAULT_POLICY_ID)
                .build();

        policyDefinitionService.create(policyDefinition)
                .onSuccess(pd -> monitor.info("Default policy definition '%s' created successfully"
                        .formatted(DEFAULT_POLICY_ID)))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Default policy definition '%s' already exists"
                                .formatted(DEFAULT_POLICY_ID));
                    } else {
                        monitor.warning("Unable to create policy definition '%s'. Reason: %s"
                                .formatted(DEFAULT_POLICY_ID, serviceFailure.getReason()));
                    }
                });

    }

    private void createPolicyDefinition(OaebudtPolicyType oaebudtPolicyType) {
        Duty duty = Duty.Builder.newInstance()
                .action(Action.Builder.newInstance()
                        .type(oaebudtPolicyType.getAction())
                        .build())
                .constraint(AtomicConstraint.Builder.newInstance()
                        .leftExpression(new LiteralExpression(oaebudtPolicyType.getLeftOperand()))
                        .operator(oaebudtPolicyType.getOperator())
                        .rightExpression(new LiteralExpression(oaebudtPolicyType.getRightOperand()))
                        .build())
                .build();

        Policy policy = Policy.Builder.newInstance()
                .permissions(Collections.emptyList())
                .prohibitions(Collections.emptyList())
                .duty(duty)
                .type(oaebudtPolicyType.getType())
                .build();

        PolicyDefinition policyDefinition = PolicyDefinition.Builder.newInstance()
                .policy(policy)
                .id(oaebudtPolicyType.getId())
                .build();

        policyDefinitionService.create(policyDefinition)
                .onSuccess(pd -> monitor.info("Default policy definition '%s' created successfully"
                        .formatted(oaebudtPolicyType.getId())))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Default policy definition '%s' already exists"
                                .formatted(oaebudtPolicyType.getId()));
                    } else {
                        monitor.warning("Unable to create policy definition '%s'. Reason: %s"
                                .formatted(oaebudtPolicyType.getId(), serviceFailure.getReason()));
                    }
                });
    }


    @Settings
    public record ReportApiConfiguration(
            @Setting(key = "web.http." + REPORT + ".port", description = "Port for " + REPORT + " api context", defaultValue = "no:op")
            int port,
            @Setting(key = "web.http." + REPORT + ".path", description = "Path for " + REPORT + " api context", defaultValue = "no:op")
            String path
    ) {}

    @Settings
    public record ConsumerApiConfiguration(
            @Setting(key = "web.http." + CONSUMER + ".port", description = "Port for " + CONSUMER + " api context", defaultValue = "no:op")
            int port,
            @Setting(key = "web.http." + CONSUMER + ".path", description = "Path for " + CONSUMER + " api context", defaultValue = "no:op")
            String path
    ) {}

    @Settings
    public record MongoDbConfiguration(
            @Setting(key = "web.datasource.mongo.url" , description = "Url for mongo db", defaultValue = "no:op")
            String datasourceUrl
    ) {}

}
