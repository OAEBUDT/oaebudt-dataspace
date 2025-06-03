package org.oaebudt.edc.web;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.connector.controlplane.services.spi.policydefinition.PolicyDefinitionService;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;
import org.oaebudt.edc.web.repository.ReportStore;
import org.oaebudt.edc.web.repository.MongoReportStoreImpl;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;
import org.oaebudt.edc.web.service.ParticipantGroupService;
import org.oaebudt.edc.web.service.ReportService;


import java.net.URI;

import static java.lang.String.format;
import static org.oaebudt.edc.web.WebApiExtension.NAME;

@Extension(NAME)
public class WebApiExtension implements ServiceExtension {
    public static final String NAME = "Web Service Extension";
    private static final String WEB = "web";
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

    @Inject
    private ParticipantGroupStore participantGroupStore;


    private Monitor monitor;

    @Override
    public void initialize(final ServiceExtensionContext context) {
        this.monitor = context.getMonitor();

        URI consumerApiBaseUrl = URI.create(format("http://%s:%s%s/", hostname.get(), consumerApiConfiguration.port(), consumerApiConfiguration.path()));

        MongoClient client = MongoClients.create(mongoDbConfiguration.datasourceUrl());
        ReportStore reportStore = new MongoReportStoreImpl(client);

        ParticipantGroupService participantGroupService = new ParticipantGroupService(participantGroupStore, policyDefinitionService, monitor);
        ReportService reportService = new ReportService(context.getMonitor(), reportStore, assetService, contractDefinitionService, consumerApiBaseUrl);

        portMappingRegistry.register(new PortMapping(WEB, reportApiConfiguration.port(), reportApiConfiguration.path()));
        portMappingRegistry.register(new PortMapping(CONSUMER, consumerApiConfiguration.port(), consumerApiConfiguration.path()));

        webService.registerResource(WEB, new ReportApiController(reportService));
        webService.registerResource(WEB, new ParticipantGroupApiController(participantGroupService, monitor));
        webService.registerResource(CONSUMER, new ConsumerApiController(reportStore));
    }

    @Settings
    public record ReportApiConfiguration(
            @Setting(key = "web.http." + WEB + ".port", description = "Port for " + WEB + " api context", defaultValue = "no:op")
            int port,
            @Setting(key = "web.http." + WEB + ".path", description = "Path for " + WEB + " api context", defaultValue = "no:op")
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
