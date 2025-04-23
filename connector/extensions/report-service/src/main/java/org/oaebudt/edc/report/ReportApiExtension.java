package org.oaebudt.edc.report;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.connector.controlplane.services.spi.policydefinition.PolicyDefinitionService;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.policy.model.Prohibition;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;
import org.oaebudt.edc.report.model.ReportType;
import org.oaebudt.edc.report.repository.ReportStore;
import org.oaebudt.edc.report.repository.MongoReportStoreImpl;


import java.util.Collections;

import static org.oaebudt.edc.report.ReportApiExtension.NAME;

@Extension(NAME)
public class ReportApiExtension implements ServiceExtension {
    public static final String NAME = "Report API";
    private static final String REPORT = "report";

    @Configuration
    private ReportApiConfiguration reportApiConfiguration;

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

    private Monitor monitor;

    @Override
    public void initialize(final ServiceExtensionContext context) {
        this.monitor = context.getMonitor();

        MongoClient client = MongoClients.create(mongoDbConfiguration.datasourceUrl());
        ReportStore reportStore = new MongoReportStoreImpl(client);

        portMappingRegistry.register(new PortMapping(REPORT, reportApiConfiguration.port(), reportApiConfiguration.path()));
        webService.registerResource(REPORT, new ReportApiController(
                context.getMonitor(),
                reportStore, assetService));
    }

    @Override
    public void start() {
        // seed default policies and contract definitions for all report types
        for(ReportType reportType : ReportType.values()) {
            createDefaultPolicyDefinition(reportType);
            createDefaultContractDefinition(reportType);
        }
    }

    private void createDefaultPolicyDefinition(ReportType reportType) {
        JsonArray emptyArray = Json.createArrayBuilder().build();
        Policy policy = Policy.Builder.newInstance()
                .permission(new Permission())
                .prohibition(new Prohibition())
                .extensibleProperty("obligation", emptyArray)
                .type(PolicyType.SET)
                .build();

        PolicyDefinition policyDefinition = PolicyDefinition.Builder.newInstance()
                .policy(policy)
                .id(reportType.name())
                .build();

        policyDefinitionService.create(policyDefinition)
                .onSuccess(pd -> monitor.info("Default policy definition %s created successfully".formatted(reportType.name())))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Default policy definition '%s' already exists".formatted(reportType.name()));
                    } else {
                        monitor.warning("Unable to create policy definition '%s'. Reason: %s".formatted(
                                reportType.name(), serviceFailure.getReason()));
                    }
                });

    }

    private void createDefaultContractDefinition(ReportType reportType) {
        ContractDefinition contractDefinition = ContractDefinition.Builder.newInstance()
                .id(reportType.name())
                .accessPolicyId(reportType.name())
                .contractPolicyId(reportType.name())
                .assetsSelector(Collections.emptyList())
                .build();

        contractDefinitionService.create(contractDefinition)
                .onSuccess(pd -> monitor.info("Default contract definition '%s' created successfully".formatted(reportType.name())))
                .onFailure(serviceFailure -> {
                    if(serviceFailure.getReason().equals(ServiceFailure.Reason.CONFLICT)) {
                        monitor.info("Default contract definition '%s' already exists".formatted(reportType.name()));
                    } else {
                        monitor.warning("Unable to create contract definition '%s'. Reason: %s".formatted(
                                reportType.name(), serviceFailure.getReason()));
                    }
                });
    }


    @Settings
    record ReportApiConfiguration(
            @Setting(key = "web.http." + REPORT + ".port", description = "Port for " + REPORT + " api context", defaultValue = "no:op")
            int port,
            @Setting(key = "web.http." + REPORT + ".path", description = "Path for " + REPORT + " api context", defaultValue = "no:op")
            String path
    ) {}

    @Settings
    record MongoDbConfiguration(
            @Setting(key = "web.datasource.mongo.url" , description = "Url for mongo db", defaultValue = "no:op")
            String datasourceUrl
    ) {}

}
