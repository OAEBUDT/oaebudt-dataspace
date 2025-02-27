package oaebudt.dataspace.connector;


import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.util.io.Ports.getFreePort;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.util.Map;
import java.util.UUID;
import oaebudt.dataspace.connector.utils.OaebudtParticipant;
import org.awaitility.Awaitility;
import org.eclipse.edc.connector.controlplane.test.system.utils.PolicyFixtures;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.spi.constants.CoreConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockserver.integration.ClientAndServer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;


@Testcontainers
@EndToEndTest
public class ManagementApiTransferTest {

    private static final String VAULT_IMAGE_NAME = "hashicorp/vault:latest";
    private static final String VAULT_TOKEN = "root";
    private static final int VAULT_PORT = 8200;
    private static final int POSTGRES_PORT = 5432;
    private static final String CONNECTOR_MODULE_PATH = ":launcher:runtime-embedded";

    private static final OaebudtParticipant PROVIDER = OaebudtParticipant.Builder.newInstance()
            .id("provider").name("provider")
            .build();

    private static final OaebudtParticipant CONSUMER = OaebudtParticipant.Builder.newInstance()
            .id("consumer").name("consumer")
            .build();

    @Container
    protected static VaultContainer<?> vaultContainer = new VaultContainer<>(DockerImageName.parse(VAULT_IMAGE_NAME))
            .withExposedPorts(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN);

    @Container
    protected static PostgreSQLContainer<?> providerPostgresContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("db")
            .withExposedPorts(POSTGRES_PORT)
            .withUsername("password")
            .withPassword("password");

    @Container
    protected static PostgreSQLContainer<?> consumerPostgresContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("db")
            .withExposedPorts(POSTGRES_PORT)
            .withUsername("password")
            .withPassword("password");

    @RegisterExtension
    static RuntimeExtension consumer = new RuntimePerClassExtension(
        new EmbeddedRuntime("consumer", CONNECTOR_MODULE_PATH)
                .configurationProvider(() -> CONSUMER.getConfiguration(getVaultPort(), getPostgresPort(consumerPostgresContainer))));

    @RegisterExtension
    protected static RuntimeExtension provider = new RuntimePerClassExtension(
            new EmbeddedRuntime("provider", CONNECTOR_MODULE_PATH)
                    .configurationProvider(() -> PROVIDER.getConfiguration(getVaultPort(), getPostgresPort(providerPostgresContainer))));

    @Test
    public void shouldSupportPushTransfer() {

        final var providerDataSource = ClientAndServer.startClientAndServer(getFreePort());
        providerDataSource.when(request("/source")).respond(response("data"));
        final var consumerDataDestination = ClientAndServer.startClientAndServer(getFreePort());
        consumerDataDestination.when(request("/destination")).respond(response());

        final Map<String, Object> dataAddressProperties = Map.of(
                 "type", "HttpData",
                "baseUrl", "http://localhost:%s/source".formatted(providerDataSource.getPort())
        );
        final String assetId = createProviderAsset(dataAddressProperties);

        CONSUMER.requestAssetFrom(assetId, PROVIDER)
                .withTransferType("HttpData-PUSH")
                .withDestination(httpDataAddress("http://localhost:" + consumerDataDestination.getPort() + "/destination"))
                .execute();

        Awaitility.await().untilAsserted(() -> {
            providerDataSource.verify(request("/source").withMethod("GET"));
            consumerDataDestination.verify(request("/destination").withBody(binary("data".getBytes())));
        });

        consumerDataDestination.stop();
        providerDataSource.stop();

    }

    private String createProviderAsset(final Map<String, Object> dataAddressProperties) {
        final var assetId = UUID.randomUUID().toString();

        final var noConstraintPolicyId = PROVIDER.createPolicyDefinition(PolicyFixtures.noConstraintPolicy());
        PROVIDER.createAsset(assetId, Map.of("name", "description"), dataAddressProperties);
        PROVIDER.createContractDefinition(assetId, UUID.randomUUID().toString(), noConstraintPolicyId, noConstraintPolicyId);
        return assetId;
    }

    private JsonObject httpDataAddress(final String baseUrl) {
        return Json.createObjectBuilder()
                .add(TYPE, CoreConstants.EDC_NAMESPACE + "DataAddress")
                .add(CoreConstants.EDC_NAMESPACE + "type", "HttpData")
                .add(CoreConstants.EDC_NAMESPACE + "baseUrl", baseUrl)
                .build();
    }

    private static int getVaultPort() {

        if (!vaultContainer.isRunning()) {
            vaultContainer.start();
        }

        return vaultContainer.getMappedPort(VAULT_PORT);
    }

    private static int getPostgresPort(final PostgreSQLContainer<?> postgres) {

        if (!postgres.isRunning()) {
            postgres.start();
        }

        return postgres.getFirstMappedPort();
    }


}
