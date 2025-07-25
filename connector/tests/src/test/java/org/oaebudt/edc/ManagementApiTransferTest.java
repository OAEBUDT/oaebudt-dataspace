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

package org.oaebudt.edc;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.util.io.Ports.getFreePort;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyString;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.json.JsonObjectBuilder;
import org.assertj.core.api.Assertions;
import org.eclipse.edc.connector.controlplane.test.system.utils.PolicyFixtures;
import org.eclipse.edc.identityhub.tests.fixtures.credentialservice.IdentityHubExtension;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockserver.integration.ClientAndServer;
import org.oaebudt.edc.utils.HashiCorpVaultEndToEndExtension;
import org.oaebudt.edc.utils.IssuerEndToEndEntension;
import org.oaebudt.edc.utils.KeycloakEndToEndExtension;
import org.oaebudt.edc.utils.MongoEndToEndExtension;
import org.oaebudt.edc.utils.OaebudtParticipant;
import org.oaebudt.edc.utils.PostgresEndToEndExtension;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@EndToEndTest
class ManagementApiTransferTest {

    public static final String DCAT_TYPE = "[0].@type";

    public static final String CATALOG = "dcat:Catalog";

    public static final String DATASET_ASSET_ID = "[0].'dcat:dataset'.@id";

    private static final String CONNECTOR_MODULE_PATH = ":launchers:runtime-embedded";

    private static final String IDENTITY_HUB_MODULE_PATH = ":launchers:identity-hub";

    ObjectMapper objectMapper = new ObjectMapper();

    private final String metadata = buildMetadata();

    private static final OaebudtParticipant PROVIDER = OaebudtParticipant.Builder.newInstance()
            .id("did:web:localhost%3A6100").name("provider")
            .build();

    private static final OaebudtParticipant CONSUMER = OaebudtParticipant.Builder.newInstance()
            .id("did:web:localhost%3A6200").name("consumer")
            .build();

    private static final OaebudtParticipant IDENTITY_HUB_PROVIDER = OaebudtParticipant.Builder.newInstance()
            .id("did:web:localhost%3A6100").name("identityhub_provider")
            .build();

    private static final OaebudtParticipant IDENTITY_HUB_CONSUMER = OaebudtParticipant.Builder.newInstance()
            .id("did:web:localhost%3A6200").name("identityhub_consumer")
            .build();

    @Order(0)
    @RegisterExtension
    static final PostgresEndToEndExtension PROVIDER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(1)
    @RegisterExtension
    static final PostgresEndToEndExtension CONSUMER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(1)
    @RegisterExtension
    static final MongoEndToEndExtension PROVIDER_MONGO_EXTENSION = new MongoEndToEndExtension();

    @Order(1)
    @RegisterExtension
    static final MongoEndToEndExtension CONSUMER_MONGO_EXTENSION = new MongoEndToEndExtension();

    @Order(2)
    @RegisterExtension
    static final PostgresEndToEndExtension PROVIDER_FC_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(3)
    @RegisterExtension
    static final PostgresEndToEndExtension IDENTITY_HUB_PROVIDER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(4)
    @RegisterExtension
    static final PostgresEndToEndExtension IDENTITY_HUB_CONSUMER_POSTGRESQL_EXTENSION = new PostgresEndToEndExtension();

    @Order(5)
    @RegisterExtension
    static final HashiCorpVaultEndToEndExtension VAULT_EXTENSION = new HashiCorpVaultEndToEndExtension();

    @Order(6)
    @RegisterExtension
    static final KeycloakEndToEndExtension KEYCLOAK_EXTENSION = new KeycloakEndToEndExtension();

    @Order(7)
    @RegisterExtension
    static final IssuerEndToEndEntension NGINX_EXTENSION = new IssuerEndToEndEntension();

    @Order(8)
    @RegisterExtension
    static final BeforeAllCallback CREATE_DATABASES = context -> {
        PROVIDER_POSTGRESQL_EXTENSION.createDatabase(PROVIDER.getName());
        CONSUMER_POSTGRESQL_EXTENSION.createDatabase(CONSUMER.getName());
        IDENTITY_HUB_PROVIDER_POSTGRESQL_EXTENSION.createDatabase(IDENTITY_HUB_PROVIDER.getName());
        IDENTITY_HUB_CONSUMER_POSTGRESQL_EXTENSION.createDatabase(IDENTITY_HUB_CONSUMER.getName());
    };

    @Order(9)
    @RegisterExtension
    static final IdentityHubExtension identityhub_provider = IdentityHubExtension.Builder.newInstance()
            .id("identityhub_provider")
            .name(IDENTITY_HUB_PROVIDER.getName())
            .modules(IDENTITY_HUB_MODULE_PATH)
            .configurationProvider(() ->  IDENTITY_HUB_PROVIDER.getIdentityHubConfiguration
                    (PROVIDER.getName(), "6100"))
            .configurationProvider(() -> IDENTITY_HUB_PROVIDER_POSTGRESQL_EXTENSION.configFor(IDENTITY_HUB_PROVIDER.getName()))
            .configurationProvider(VAULT_EXTENSION::config)
            .build();

    @Order(10)
    @RegisterExtension
    static final IdentityHubExtension identityhub_consumer = IdentityHubExtension.Builder.newInstance()
            .id("identityhub_consumer")
            .name(IDENTITY_HUB_CONSUMER.getName())
            .modules(IDENTITY_HUB_MODULE_PATH)
            .configurationProvider(() ->  IDENTITY_HUB_CONSUMER.getIdentityHubConfiguration
                    (CONSUMER.getName(), "6200"))
            .configurationProvider(() -> IDENTITY_HUB_CONSUMER_POSTGRESQL_EXTENSION.configFor(IDENTITY_HUB_CONSUMER.getName()))
            .configurationProvider(VAULT_EXTENSION::config)
            .build();

    @Order(11)
    @RegisterExtension
    protected static RuntimeExtension provider = new RuntimePerClassExtension(
            new EmbeddedRuntime("provider", CONNECTOR_MODULE_PATH)
                    .configurationProvider(() -> PROVIDER.getConfiguration(IDENTITY_HUB_PROVIDER.getIdentityHubStsPort()))
                    .configurationProvider(() -> PROVIDER_POSTGRESQL_EXTENSION.configFor(PROVIDER.getName()))
                    .configurationProvider(VAULT_EXTENSION::config)
                    .configurationProvider(KEYCLOAK_EXTENSION::config)
                    .configurationProvider(PROVIDER_MONGO_EXTENSION::config)
                    .registerSystemExtension(ServiceExtension.class, PROVIDER.seedVaultKeys()));

    @Order(12)
    @RegisterExtension
    static RuntimeExtension consumer = new RuntimePerClassExtension(
        new EmbeddedRuntime("consumer", CONNECTOR_MODULE_PATH)
                .configurationProvider(() ->  CONSUMER.getConfiguration(IDENTITY_HUB_CONSUMER.getIdentityHubStsPort()))
                .configurationProvider(() -> CONSUMER_POSTGRESQL_EXTENSION.configFor(CONSUMER.getName()))
                .configurationProvider(VAULT_EXTENSION::config)
                .configurationProvider(KEYCLOAK_EXTENSION::config)
                .configurationProvider(CONSUMER_MONGO_EXTENSION::config));

    @BeforeAll
    public static void setup() {

        int providerProtocolPort = PROVIDER.getConnectorProtocolUri().getPort();
        int providerCredentialsPort = IDENTITY_HUB_PROVIDER.getIdentityHubCredentialsApiUri().getPort();
        int consumerProtocolPort = CONSUMER.getConnectorProtocolUri().getPort();
        int consumerCredentialsPort = IDENTITY_HUB_CONSUMER.getIdentityHubCredentialsApiUri().getPort();

        String providerManifest = createParticipantManifest(providerProtocolPort, providerCredentialsPort, PROVIDER.getId(), PROVIDER.getName());
        String consumerManifest = createParticipantManifest(consumerProtocolPort, consumerCredentialsPort, CONSUMER.getId(), CONSUMER.getName());

        createIdentityHubParticipant(IDENTITY_HUB_PROVIDER, providerManifest, OaebudtParticipant.IH_API_SUPERUSER_KEY);
        createIdentityHubParticipant(IDENTITY_HUB_CONSUMER, consumerManifest, OaebudtParticipant.IH_API_SUPERUSER_KEY);

        PROVIDER.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());
        CONSUMER.setAuthorizationToken(KEYCLOAK_EXTENSION.getToken());
    }

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
    
    @Disabled("Skipping this test because its flaky")
    @Test
    public void shouldGetContractOfferViaFederatedCatalog() {

        final Map<String, Object> dataAddressProperties = Map.of(
                "type", "HttpData-PULL",
                "baseUrl", "http://localhost:8080/source"
        );
        final String assetId = createProviderAsset(PROVIDER, dataAddressProperties);

        final JsonObject requestBody = Json.createObjectBuilder()
                .add("@context", Json.createObjectBuilder()
                        .add("@vocab", "https://w3id.org/edc/v0.0.1/ns/"))
                .add("@type", "QuerySpec")
                .build();

        await().untilAsserted(() ->
                given()
                        .baseUri(CONSUMER.getCatalogUrl().toString())
                        .contentType(ContentType.JSON).body(requestBody)
                        .headers(OaebudtParticipant.API_KEY_HEADER_KEY, OaebudtParticipant.API_KEY_HEADER_VALUE)
                        .when()
                        .post("/v1alpha/catalog/query")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(HttpStatus.SC_OK)
                        .body(DCAT_TYPE, not(emptyString()))
                        .body(DCAT_TYPE, is(CATALOG))
                        .body(DATASET_ASSET_ID, anyOf(
                                is(assetId), // for String
                                hasItem(assetId) // for List
                        )));
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

    @Test
    public void shouldUploadReportAndConsumerShouldConsumeReport() throws IOException {
        String reportUri = PROVIDER.getWebServiceUrl().get().toString() + "report/upload";
        String participantUri = PROVIDER.getWebServiceUrl().get().toString() + "participant/group";
        String accessToken = KEYCLOAK_EXTENSION.getToken();
        final var consumerEdrReceiver = ClientAndServer.startClientAndServer(getFreePort());
        consumerEdrReceiver.when(request("/edr")).respond(response());

        Map<String, Object> jsonBodyMap = Map.of(
                "groupName", "friends",
                "participants", List.of(CONSUMER.getId())
        );

        String jsonBody = objectMapper.writeValueAsString(jsonBodyMap);

        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header(HttpHeaderNames.AUTHORIZATION, "Bearer " + accessToken)
                .body(jsonBody)
                .when()
                .post(participantUri)
                .then()
                .statusCode(201);

        Map<String, Object> jsonContentMap = Map.of(
                "userId", 123,
                "status", "active"
        );

        String jsonContent = objectMapper.writeValueAsString(jsonContentMap);

        Response response = RestAssured
                .given()
                .header(HttpHeaderNames.AUTHORIZATION, "Bearer " + accessToken)
                .multiPart(
                        "file",                             // form field for file
                        "data.json",                        // pretend filename
                        new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8)),
                        "application/json"
                )
                .multiPart("reportType", "TITLE_REPORT")
                .multiPart("title", "Report_IR")
                .multiPart("metadata", metadata)
                .multiPart("accessDefinition", "allow-friends")// additional form data
                .when()
                .post(reportUri)
                .then().statusCode(201)
                .extract().response();

        String assetId = response.jsonPath().getString("assetId");


        JsonArray jsonArray = CONSUMER.getCatalogDatasets(PROVIDER);

        Assertions.assertThat(jsonArray.toString()).contains("TITLE_REPORT");
        Assertions.assertThat(jsonArray.toString()).contains("legalOrganizationName");
        Assertions.assertThat(jsonArray.toString()).contains("organizationWebsite");
        Assertions.assertThat(jsonArray.toString()).contains("http://valid.com");
        Assertions.assertThat(jsonArray.toString()).contains("contactEmail");
        Assertions.assertThat(jsonArray.toString()).contains("dataAccuracyLevel");
        Assertions.assertThat(jsonArray.toString()).contains("dataGenerationTransparencyLevel");
        Assertions.assertThat(jsonArray.toString()).contains("dataDeliveryReliabilityLevel");
        Assertions.assertThat(jsonArray.toString()).contains("dataFrequencyLevel");

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
                .extract().body().jsonPath();

        Assertions.assertThat(body.getString("status")).isEqualTo("active");
        Assertions.assertThat(body.getString("reportType")).isEqualTo("TITLE_REPORT");

        consumerEdrReceiver.stop();
    }
    @Test
    public void shouldUploadReportAndConsumerShouldAllowMultipleSameTypeReportInCatalog() throws IOException {
        String reportUri = PROVIDER.getWebServiceUrl().get().toString() + "report/upload";
        String participantUri = PROVIDER.getWebServiceUrl().get().toString() + "participant/group";
        String accessToken = KEYCLOAK_EXTENSION.getToken();
        final var consumerEdrReceiver = ClientAndServer.startClientAndServer(getFreePort());
        consumerEdrReceiver.when(request("/edr")).respond(response());

        Map<String, Object> jsonBodyMap = Map.of(
                "groupName", "friends",
                "participants", List.of(CONSUMER.getId())
        );

        String jsonBody = objectMapper.writeValueAsString(jsonBodyMap);

        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header(HttpHeaderNames.AUTHORIZATION, "Bearer " + accessToken)
                .body(jsonBody)
                .when()
                .post(participantUri)
                .then()
                .statusCode(201);

        Map<String, Object> jsonContentMap = Map.of(
                "userId", 123,
                "status", "active"
        );

        String jsonContent = objectMapper.writeValueAsString(jsonContentMap);

        uploadReportMultipart(accessToken, jsonContent, metadata, reportUri);
        uploadReportMultipart(accessToken, jsonContent, metadata, reportUri);


        JsonArray datasets = CONSUMER.getCatalogDatasets(PROVIDER);

        Assertions.assertThat(datasets)
                .map(JsonObject.class::cast)
                .map(obj -> obj.getString("@id", ""))
                .anyMatch(it -> it.startsWith("TITLE_REPORT"));
    }

    private static void uploadReportMultipart(String accessToken, String jsonContent, String metadata, String reportUri) {
        RestAssured
                .given()
                .header(HttpHeaderNames.AUTHORIZATION, "Bearer " + accessToken)
                .multiPart(
                        "file",
                        "data.json",
                        new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8)),
                        "application/json"
                )
                .multiPart("reportType", "TITLE_REPORT")
                .multiPart("title", "Report_IR")
                .multiPart("metadata", metadata)
                .multiPart("accessDefinition", "allow-friends")
                .when()
                .post(reportUri)
                .then()
                .statusCode(201);
    }

    @Test
    public void shouldUploadReportAndConsumerShouldNotConsumeReport_InvalidAccessLevel() throws JsonProcessingException {
        String reportUri =PROVIDER.getWebServiceUrl().get().toString() + "report/upload";
        String participantUri = PROVIDER.getWebServiceUrl().get().toString() + "participant/group";
        String accessToken = KEYCLOAK_EXTENSION.getToken();

        Map<String, Object> jsonMap = Map.of(
                "groupName", "nobody",
                "participants", List.of()
        );

        String allowNobodyJsonBody = objectMapper.writeValueAsString(jsonMap);

        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header(HttpHeaderNames.AUTHORIZATION, "Bearer " + accessToken)
                .body(allowNobodyJsonBody)
                .when()
                .post(participantUri)
                .then()
                .statusCode(201);


        Map<String, Object> jsonContentMap = Map.of(
                "userId", 123,
                "status", "active"
        );

        String jsonContent = objectMapper.writeValueAsString(jsonContentMap);

        RestAssured
                .given()
                .header(HttpHeaderNames.AUTHORIZATION, "Bearer " + accessToken)
                .multiPart(
                        "file",                             // form field for file
                        "data.json",                        // pretend filename
                        new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8)),
                        "application/json"
                )
                .multiPart("reportType", "ITEM_REPORT")
                .multiPart("title", "Report_IR")
                .multiPart("metadata", metadata)
                .multiPart("accessDefinition", "allow-nobody")// additional form data
                .when()
                .post(reportUri)
                .then()
                .statusCode(201);

        JsonObjectBuilder requestBodyBuilder = Json.createObjectBuilder()
                .add("@context", Json.createObjectBuilder().add("@vocab", "https://w3id.org/edc/v0.0.1/ns/"))
                .add("@type", "CatalogRequest").add("counterPartyId", PROVIDER.getId())
                .add("counterPartyAddress", PROVIDER.getProtocolUrl())
                .add("protocol", "dataspace-protocol-http");

        String response = CONSUMER.baseManagementRequest()
                        .contentType(ContentType.JSON)
                        .when()
                        .body(requestBodyBuilder.build())
                        .post("/v3/catalog/request", new Object[0])
                        .then().log().ifError()
                        .statusCode(200)
                        .extract().body().asString();

        Assertions.assertThat(response.contains("ITEM_REPORT")).isFalse();

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

    private static String createParticipantManifest(int protocolPort, int credentialsPort, String participantId, String participantName) {
        String base64ParticipantId = encodeBase64(participantId);

        JsonArrayBuilder serviceEndpointsBuilder = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("type", "CredentialService")
                        .add("serviceEndpoint", String.format("http://localhost:%d/api/credentials/v1/participants/%s", credentialsPort, base64ParticipantId))
                        .add("id", String.format("%s-credentialservice-1", participantName)))
                .add(Json.createObjectBuilder()
                        .add("type", "ProtocolEndpoint")
                        .add("serviceEndpoint", String.format("http://localhost:%d/protocol", protocolPort))
                        .add("id", String.format("%s-dsp", participantName)));

        JsonObject keyObject = Json.createObjectBuilder()
                .add("keyId", String.format("%s#key-1", participantId))
                .add("privateKeyAlias", String.format("%s#key-1", participantId))
                .add("keyGeneratorParams", Json.createObjectBuilder()
                        .add("algorithm", "EC")
                        .build())
                .build();

        JsonObject participantManifest = Json.createObjectBuilder()
                .add("roles", Json.createArrayBuilder().build()) // Empty array for roles
                .add("serviceEndpoints", serviceEndpointsBuilder)
                .add("active", true)
                .add("participantId", participantId)
                .add("did", participantId)
                .add("key", keyObject)
                .build();

        return participantManifest.toString();
    }

    private static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    private static void createIdentityHubParticipant(OaebudtParticipant participant, String body, String apiKey) {
        String apiUrl =  participant.getIdentityHubApiUri() + "/v1alpha/participants";

        RequestSpecification request = RestAssured.given();
        request
            .header(OaebudtParticipant.API_KEY_HEADER_KEY, apiKey)
            .contentType(ContentType.JSON)
            .log().all()
            .body(body)
            .post(apiUrl)
            .then()
            .log().ifValidationFails()
            .extract().response();
    }

    private static String buildMetadata() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode metadata = mapper.createObjectNode();

        metadata.put("legalOrganizationName", "Org");
        metadata.put("countryOfOrganization", "US");
        metadata.put("contactPerson", "Jane");
        metadata.put("dataProcessingDescription", "desc");
        metadata.put("qualityAssuranceMeasures", "qa");
        metadata.put("dataLicensingTerms", "terms");
        metadata.put("organizationWebsite", "http://valid.com");
        metadata.put("contactEmail", "valid@example.com");
        metadata.put("dataAccuracyLevel", 1);
        metadata.put("dataGenerationTransparencyLevel", 1);
        metadata.put("dataDeliveryReliabilityLevel", 1);
        metadata.put("dataFrequencyLevel", 1);
        metadata.put("dataGranularityLevel", 1);
        metadata.put("dataConsistencyLevel", 1);

        try {
            return mapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON metadata", e);
        }
    }
}
