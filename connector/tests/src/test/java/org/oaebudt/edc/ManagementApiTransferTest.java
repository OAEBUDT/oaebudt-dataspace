package org.oaebudt.edc;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.util.io.Ports.getFreePort;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import io.restassured.http.ContentType;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import org.oaebudt.edc.keycloak.KeyCloakAuthenticationExtension;
import org.oaebudt.edc.utils.HashiCorpVaultEndToEndExtension;
import org.oaebudt.edc.utils.KeycloakEndToEndExtension;
import org.oaebudt.edc.utils.OaebudtParticipant;
import org.oaebudt.edc.utils.PostgresEndToEndExtension;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@EndToEndTest
class ManagementApiTransferTest {

    public static final String DCAT_TYPE = "[0].@type";
    public static final String CATALOG = "dcat:Catalog";
    public static final String DATASET_ASSET_ID = "[0].'dcat:dataset'.@id";

    private static final String CONNECTOR_MODULE_PATH = ":launcher:runtime-embedded";

    private static final OaebudtParticipant PROVIDER = OaebudtParticipant.Builder.newInstance()
            .id("provider").name("provider")
            .build();

    private static final OaebudtParticipant CONSUMER = OaebudtParticipant.Builder.newInstance()
            .id("consumer").name("consumer")
            .build();

    private static final OaebudtParticipant PROVIDER_FC = OaebudtParticipant.Builder.newInstance()
            .id("provider_fc").name("provider_fc")
            .build();

    @Order(0)
    @RegisterExtension
    static final PostgresEndToEndExtension PROVIDER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(1)
    @RegisterExtension
    static final PostgresEndToEndExtension CONSUMER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(2)
    @RegisterExtension
    static final PostgresEndToEndExtension PROVIDER_FC_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(3)
    @RegisterExtension
    static final BeforeAllCallback CREATE_DATABASES = context -> {
        PROVIDER_POSTGRESQL_EXTENSION.createDatabase(PROVIDER.getName());
        CONSUMER_POSTGRESQL_EXTENSION.createDatabase(CONSUMER.getName());
        PROVIDER_FC_POSTGRESQL_EXTENSION.createDatabase(PROVIDER_FC.getName());
    };

    @Order(4)
    @RegisterExtension
    static final HashiCorpVaultEndToEndExtension VAULT_EXTENSION = new HashiCorpVaultEndToEndExtension();

    @Order(5)
    @RegisterExtension
    static final KeycloakEndToEndExtension KEYCLOAK_EXTENSION = new KeycloakEndToEndExtension();


    @RegisterExtension
    static RuntimeExtension consumer = new RuntimePerClassExtension(
        new EmbeddedRuntime("consumer", CONNECTOR_MODULE_PATH)
                .configurationProvider(CONSUMER::getConfiguration)
                .configurationProvider(() -> CONSUMER_POSTGRESQL_EXTENSION.configFor(CONSUMER.getName()))
                .configurationProvider(VAULT_EXTENSION::config)
                .configurationProvider(KEYCLOAK_EXTENSION::config)
                .registerSystemExtension(ServiceExtension.class, new KeyCloakAuthenticationExtension()));

    @RegisterExtension
    protected static RuntimeExtension provider = new RuntimePerClassExtension(
            new EmbeddedRuntime("provider", CONNECTOR_MODULE_PATH)
                    .configurationProvider(PROVIDER::getConfiguration)
                    .configurationProvider(() -> PROVIDER_POSTGRESQL_EXTENSION.configFor(PROVIDER.getName()))
                    .configurationProvider(VAULT_EXTENSION::config)
                    .configurationProvider(KEYCLOAK_EXTENSION::config)
                    .registerSystemExtension(ServiceExtension.class, new KeyCloakAuthenticationExtension())
                    .registerSystemExtension(ServiceExtension.class, PROVIDER.seedVaultKeys()));

    @RegisterExtension
    protected static RuntimeExtension providerFc = new RuntimePerClassExtension( //For fedearted catalog test
            new EmbeddedRuntime("provider_fc", CONNECTOR_MODULE_PATH)
                    .configurationProvider(PROVIDER_FC::getConfiguration)
                    .configurationProvider(() -> PROVIDER_FC_POSTGRESQL_EXTENSION.configFor(PROVIDER_FC.getName()))
                    .configurationProvider(VAULT_EXTENSION::config)
                    .configurationProvider(KEYCLOAK_EXTENSION::config)
                    .registerSystemExtension(ServiceExtension.class, new KeyCloakAuthenticationExtension()));

    @Test
    public void shouldSupportPushTransfer() {
        PROVIDER.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());
        CONSUMER.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());

        final var providerDataSource = ClientAndServer.startClientAndServer(getFreePort());
        providerDataSource.when(request("/source")).respond(response("data"));
        final var consumerDataDestination = ClientAndServer.startClientAndServer(getFreePort());
        consumerDataDestination.when(request("/destination")).respond(response());

        final Map<String, Object> dataAddressProperties = Map.of(
                 "type", "HttpData",
                "baseUrl", "http://localhost:%s/source".formatted(providerDataSource.getPort())
        );
        final String assetId = createProviderAsset(PROVIDER, dataAddressProperties);

        CONSUMER.requestAssetFrom(assetId, PROVIDER)
                .withTransferType("HttpData-PUSH")
                .withDestination(httpDataAddress("http://localhost:" + consumerDataDestination.getPort() + "/destination"))
                .execute();

        await().untilAsserted(() -> {
            providerDataSource.verify(request("/source").withMethod("GET"));
            consumerDataDestination.verify(request("/destination").withBody(binary("data".getBytes())));
        });

        consumerDataDestination.stop();
        providerDataSource.stop();

    }

    @Test
    public void shouldSupportPullTransfer() throws IOException {
        PROVIDER.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());
        CONSUMER.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());

        final var providerDataSource = ClientAndServer.startClientAndServer(getFreePort());
        providerDataSource.when(request("/source")).respond(response("data"));
        final var consumerEdrReceiver = ClientAndServer.startClientAndServer(getFreePort());
        consumerEdrReceiver.when(request("/edr")).respond(response());

        final Map<String, Object> dataAddressProperties = Map.of(
                "type", "HttpData",
                "baseUrl", "http://localhost:%s/source".formatted(providerDataSource.getPort())
        );
        final String assetId = createProviderAsset(PROVIDER, dataAddressProperties);

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

    @Test
    public void shouldGetContractOfferViaFederatedCatalog() {
        PROVIDER_FC.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());

        final Map<String, Object> dataAddressProperties = Map.of(
                "type", "HttpData-PULL",
                "baseUrl", "http://localhost:8080/source"
        );
        final String assetId = createProviderAsset(PROVIDER_FC, dataAddressProperties);

        final JsonObject requestBody = Json.createObjectBuilder()
                .add("@context", Json.createObjectBuilder()
                        .add("@vocab", "https://w3id.org/edc/v0.0.1/ns/"))
                .add("@type", "QuerySpec")
                .build();

        await()
                .atMost(Duration.ofSeconds(200))
                .pollDelay(Duration.ofSeconds(5))
                .ignoreExceptions()
                .until(() -> given()
                                .baseUri(PROVIDER_FC.getCatalogUrl().toString())
                                .contentType(ContentType.JSON).body(requestBody)
                                .headers(OaebudtParticipant.API_KEY_HEADER_KEY, OaebudtParticipant.API_KEY_HEADER_VALUE)
                                .when()
                                .post("/v1alpha/catalog/query")
                                .then()
                                .log().ifError()
                                .statusCode(HttpStatus.SC_OK)
                                .body(DCAT_TYPE, not(emptyString()))
                                .body(DCAT_TYPE, is(CATALOG))
                                .extract()
                                .jsonPath()
                                .get(DATASET_ASSET_ID),
                        id -> id.equals(assetId));
    }

    @Test
    public void shouldFailToCreateAsset_InvalidToken() {
        PROVIDER.setAuthorizationToken("random token");

        final var providerDataSource = ClientAndServer.startClientAndServer(getFreePort());
        providerDataSource.when(request("/source")).respond(response("data"));

        final Map<String, Object> dataAddressProperties = Map.of(
                "type", "HttpData",
                "baseUrl", "http://localhost:%s/source".formatted(providerDataSource.getPort())
        );

        final var assetId = UUID.randomUUID().toString();
        final Map<String, String> properties = Map.of("name", "description");

        final JsonObject requestBody = Json.createObjectBuilder()
                .add("@context", Json.createObjectBuilder()
                        .add("@vocab", "https://w3id.org/edc/v0.0.1/ns/"))
                .add("@id", assetId).add("properties", Json.createObjectBuilder(properties))
                .add("dataAddress", Json.createObjectBuilder(dataAddressProperties))
                .build();

        PROVIDER.baseManagementRequest()
                .contentType(ContentType.JSON).body(requestBody)
                .when()
                .post("/v3/assets", new Object[0])
                .then()
                .statusCode(401);

    }

    private String createProviderAsset(OaebudtParticipant participant, final Map<String, Object> dataAddressProperties) {
        final var assetId = UUID.randomUUID().toString();

        final var noConstraintPolicyId = participant.createPolicyDefinition(PolicyFixtures.noConstraintPolicy());
        participant.createAsset(assetId, Map.of("name", "description"), dataAddressProperties);
        participant.createContractDefinition(assetId, UUID.randomUUID().toString(), noConstraintPolicyId, noConstraintPolicyId);
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
