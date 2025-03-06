package org.oaebudt.edc;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.util.io.Ports.getFreePort;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.oaebudt.edc.utils.HashiCorpVaultEndToEndExtension;
import org.oaebudt.edc.utils.OaebudtParticipant;
import org.oaebudt.edc.utils.PostgresEndToEndExtension;
import org.assertj.core.api.Assertions;
import org.eclipse.edc.connector.controlplane.test.system.utils.PolicyFixtures;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockserver.integration.ClientAndServer;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@EndToEndTest
public class ManagementApiTransferTest {

    private static final String CONNECTOR_MODULE_PATH = ":launcher:runtime-embedded";

    private static final OaebudtParticipant PROVIDER = OaebudtParticipant.Builder.newInstance()
            .id("provider").name("provider")
            .build();

    private static final OaebudtParticipant CONSUMER = OaebudtParticipant.Builder.newInstance()
            .id("consumer").name("consumer")
            .build();

    @Order(0)
    @RegisterExtension
    static final PostgresEndToEndExtension PROVIDER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(1)
    @RegisterExtension
    static final PostgresEndToEndExtension CONSUMER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(2)
    @RegisterExtension
    static final BeforeAllCallback CREATE_DATABASES = context -> {
        PROVIDER_POSTGRESQL_EXTENSION.createDatabase(PROVIDER.getName());
        CONSUMER_POSTGRESQL_EXTENSION.createDatabase(CONSUMER.getName());
    };

    @Order(3)
    @RegisterExtension
    static final HashiCorpVaultEndToEndExtension VAULT_EXTENSION = new HashiCorpVaultEndToEndExtension();

    @RegisterExtension
    static RuntimeExtension consumer = new RuntimePerClassExtension(
        new EmbeddedRuntime("consumer", CONNECTOR_MODULE_PATH)
                .configurationProvider(CONSUMER::getConfiguration)
                .configurationProvider(() -> CONSUMER_POSTGRESQL_EXTENSION.configFor(CONSUMER.getName()))
                .configurationProvider(VAULT_EXTENSION::config));

    @RegisterExtension
    protected static RuntimeExtension provider = new RuntimePerClassExtension(
            new EmbeddedRuntime("provider", CONNECTOR_MODULE_PATH)
                    .configurationProvider(PROVIDER::getConfiguration)
                    .configurationProvider(() -> PROVIDER_POSTGRESQL_EXTENSION.configFor(PROVIDER.getName()))
                    .configurationProvider(VAULT_EXTENSION::config)
                    .registerSystemExtension(ServiceExtension.class, PROVIDER.seedVaultKeys()));

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

        await().untilAsserted(() -> {
            providerDataSource.verify(request("/wrong-source").withMethod("GET"));
            consumerDataDestination.verify(request("/destination").withBody(binary("wrong data".getBytes())));
        });

        consumerDataDestination.stop();
        providerDataSource.stop();

    }

    @Test
    public void shouldSupportPullTransfer() throws IOException {
        final var providerDataSource = ClientAndServer.startClientAndServer(getFreePort());
        providerDataSource.when(request("/source")).respond(response("data"));
        final var consumerEdrReceiver = ClientAndServer.startClientAndServer(getFreePort());
        consumerEdrReceiver.when(request("/edr")).respond(response());

        final Map<String, Object> dataAddressProperties = Map.of(
                "type", "HttpData",
                "baseUrl", "http://localhost:%s/source".formatted(providerDataSource.getPort())
        );
        final String assetId = createProviderAsset(dataAddressProperties);

        final String transferProcessId = CONSUMER.requestAssetFrom(assetId, PROVIDER)
                .withTransferType("HttpData-PULL")
                .withCallbacks(Json.createArrayBuilder()
                        .add(createCallback("http://localhost:%s/edr".formatted(consumerEdrReceiver.getPort()), true, Set.of("transfer.process.started")))
                        .build())
                .execute();

        CONSUMER.awaitTransferToBeInState(transferProcessId, STARTED);

        final var edrRequests = await().until(() -> consumerEdrReceiver.retrieveRecordedRequests(request("/edr")), it -> it.length > 0);

        final var edr = new ObjectMapper().readTree(edrRequests[0].getBodyAsRawBytes()).get("payload").get("dataAddress").get("properties");

        final var endpoint = edr.get(EDC_NAMESPACE + "endpoint").asText();
        final var authCode = edr.get(EDC_NAMESPACE + "authorization").asText();

        final var body = given()
                .header("Authorization", authCode)
                .when()
                .get(endpoint)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().body().asString();

        Assertions.assertThat(body).isEqualTo("data");

        consumerEdrReceiver.stop();

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
                .add(TYPE, EDC_NAMESPACE + "DataAddress")
                .add(EDC_NAMESPACE + "type", "HttpData")
                .add(EDC_NAMESPACE + "baseUrl", baseUrl)
                .build();
    }

    public JsonObject createCallback(final String url, final boolean transactional, final Set<String> events) {
        return Json.createObjectBuilder()
                .add(TYPE,  "CallbackAddress")
                .add("transactional", transactional)
                .add("uri", url)
                .add("events", events
                        .stream()
                        .collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add)
                        .build())
                .build();
    }

}
