package org.oaebudt.edc.report;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.ApiContext;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;
import org.oaebudt.edc.report.repository.ReportStore;
import org.oaebudt.edc.report.repository.MongoReportStoreImpl;


import static org.oaebudt.edc.report.ReportApiExtension.NAME;

@Extension(NAME)
public class ReportApiExtension implements ServiceExtension {
    public static final String NAME = "Report API";
    private static final String REPORT = "report";

    @Configuration
    private ReportApiConfiguration reportApiConfiguration;

    @Configuration
    private MongoDbConfiguration mongoDbConfiguration;

    @Configuration
    private  ManagementApiConfiguration managementApiConfiguration;

    @Inject
    private WebService webService;

    @Inject
    private PortMappingRegistry portMappingRegistry;

    @Override
    public void initialize(final ServiceExtensionContext context) {
        String host = "localhost";

        MongoClient client = MongoClients.create(mongoDbConfiguration.datasourceUrl());
        ReportStore reportStore = new MongoReportStoreImpl(client);


        portMappingRegistry.register(new PortMapping(REPORT, reportApiConfiguration.port(), reportApiConfiguration.path()));
        webService.registerResource(REPORT, new ReportApiController(
                context.getMonitor(),
                reportStore,
                buildUrl(host, managementApiConfiguration.port(), managementApiConfiguration.path())));
    }

    public static String buildUrl(String host, int port, String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "http://" + host + ":" + port + path;
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

    @Settings
    record ManagementApiConfiguration(
            @Setting(key = "web.http." + ApiContext.MANAGEMENT + ".port", description = "Port for " + REPORT + " api context", defaultValue = "no:op")
            int port,
            @Setting(key = "web.http." + ApiContext.MANAGEMENT + ".path", description = "Path for " + REPORT + " api context", defaultValue = "no:op")
            String path
    ) {}

}
