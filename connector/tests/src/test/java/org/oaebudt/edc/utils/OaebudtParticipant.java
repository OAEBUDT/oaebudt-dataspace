package org.oaebudt.edc.utils;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.connector.controlplane.test.system.utils.LazySupplier;
import org.eclipse.edc.connector.controlplane.test.system.utils.Participant;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.eclipse.edc.util.io.Ports;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.security.AsymmetricKey;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.util.Map.entry;
import static org.eclipse.edc.util.io.Ports.getFreePort;

public class OaebudtParticipant extends Participant {

    public static final String API_KEY_HEADER_KEY = "X-Api-Key";

    public static final String API_KEY_HEADER_VALUE = "password";

    public static final String IH_API_SUPERUSER_KEY = "c3VwZXItdXNlcg==.K+CKuM+8XNuEfLggseLntVljpgLnRzPMNo1WT6dWU1HUJP07l50k8AUreEIy3gcYTBn4vxzMWIg+1TDPYsxpug==";

    private String token;

    protected LazySupplier<URI> catalogServerUri = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/catalog"));

    protected LazySupplier<URI> controlPlaneControl = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/control"));

    protected LazySupplier<URI> identityHubApi = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api"));

    protected LazySupplier<URI> identityHubCredentials = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api/credentials/"));

    protected LazySupplier<URI> identityHubIdentity = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api/identity"));

    protected LazySupplier<URI> identityHubPresentation = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api/presentation"));

    protected LazySupplier<URI> identityHubVersion = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api/version"));

    protected LazySupplier<URI> identityHubSts = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api/sts"));

    protected LazySupplier<URI> reportServiceUrl = new LazySupplier<>(() ->
            URI.create("http://localhost:" + Ports.getFreePort() + "/api/report"));

    private OaebudtParticipant() {}

    public void setAuthorizationToken(String token) {
        this.token = token;
    }

    public ServiceExtension seedVaultKeys() {
        return new SeedVaultKeys();
    }

    public static class Builder extends Participant.Builder<OaebudtParticipant, Builder> {

        public static Builder newInstance() {
            return new Builder(new OaebudtParticipant());
        }

        protected Builder(final OaebudtParticipant participant) {
            super(participant);
        }
    }

    public Config getConfiguration(final Integer stsPort) {
        final var map = Map.ofEntries(
                entry("edc.catalog.cache.execution.delay.seconds", 10 + ""),
                entry("edc.dpf.selector.url", "http://localhost:" + controlPlaneControl.get().getPort() + "/control/v1/dataplanes"),
                entry("edc.dsp.callback.address", controlPlaneProtocol.get().toString()),
                entry("edc.iam.did.trusted.issuers", "did:web:localhost%3A19999,did:web:localhost%3A19998"),
                entry("edc.iam.did.web.use.https", "false"),
                entry("edc.iam.issuer.id", id),
                entry("edc.iam.sts.oauth.client.id", id),
                entry("edc.iam.sts.oauth.client.secret.alias", id + "-sts-client-secret"),
                entry("edc.iam.sts.oauth.token.url", "http://localhost:" + stsPort + "/api/sts/token"),
                entry("edc.iam.sts.privatekey.alias", id + "#key-1"),
                entry("edc.iam.sts.publickey.id", id + "#key-1"),
                entry("edc.participant.id", id),
                entry("edc.runtime.id", id),
                entry("edc.transfer.proxy.token.signer.privatekey.alias", "private-key-alias"),
                entry("edc.transfer.proxy.token.verifier.publickey.alias", "public-key-alias"),
                entry("fc.participants.list", controlPlaneProtocol.get().toString()), // temp for testing crawler
                entry("web.http.catalog.path", catalogServerUri.get().getPath()),
                entry("web.http.catalog.port", catalogServerUri.get().getPort() + ""),
                entry("web.http.control.path", controlPlaneControl.get().getPath()),
                entry("web.http.control.port", controlPlaneControl.get().getPort() + ""),
                entry("web.http.management.path", controlPlaneManagement.get().getPath()),
                entry("web.http.management.port", controlPlaneManagement.get().getPort() + ""),
                entry("web.http.management.auth.type", "keycloak"),
                entry("web.http.path", "/api"),
                entry("web.http.port", getFreePort() + ""),
                entry("web.http.protocol.path", controlPlaneProtocol.get().getPath()),
                entry("web.http.protocol.port", controlPlaneProtocol.get().getPort() + ""),
                entry("web.http.public.path", "/public"),
                entry("web.http.public.port", getFreePort() + ""),
                entry("web.http.version.path", "/version"),
                entry("web.http.version.port", getFreePort() + ""),
                entry("web.http.report.port", reportServiceUrl.get().getPort() + ""),
                entry("web.http.report.path", reportServiceUrl.get().getPath()),
                entry("web.http.report.auth.type", "keycloak")
        );

        return ConfigFactory.fromMap(map);
    }

    public Config getIdentityHubConfiguration(final String participant, final String didPort) {
        final var map = Map.ofEntries(
                entry("edc.did.credentials.path", getCredentialsPath(participant)),
                entry("edc.iam.did.web.use.https", "false"),
                entry("edc.iam.sts.privatekey.alias", id + "#key-1"),
                entry("edc.iam.sts.publickey.id", id + "#key-1"),
                entry("edc.ih.api.superuser.key", IH_API_SUPERUSER_KEY),
                entry("edc.ih.iam.id", id),
                entry("web.http.credentials.path", "/api/credentials/"),
                entry("web.http.credentials.port", identityHubCredentials.get().getPort() + ""),
                entry("web.http.did.path", "/"),
                entry("web.http.did.port", didPort),
                entry("web.http.identity.path", "/api/identity"),
                entry("web.http.identity.port", identityHubIdentity.get().getPort() + ""),
                entry("web.http.path", "/api"),
                entry("web.http.port", identityHubApi.get().getPort() + ""),
                entry("web.http.presentation.path", "/api/presentation"),
                entry("web.http.presentation.port", identityHubPresentation.get().getPort() + ""),
                entry("web.http.sts.path", "/api/sts"),
                entry("web.http.sts.port", identityHubSts.get().getPort() + ""),
                entry("web.http.version.path", "/api/version"),
                entry("web.http.version.port", identityHubVersion.get().getPort() + "")
        );

        return ConfigFactory.fromMap(map);
    }

    public URI getCatalogUrl() {
        return catalogServerUri.get();
    }

    public URI getConnectorProtocolUri() {
        return controlPlaneProtocol.get();
    }

    public URI getIdentityHubApiUri() {
        return identityHubIdentity.get();
    }

    public URI getIdentityHubCredentialsApiUri() {
        return identityHubCredentials.get();
    }

    public Integer getIdentityHubStsPort() {
        return identityHubSts.get().getPort();
    }

    public LazySupplier<URI> getReportServiceUrl() {
        return reportServiceUrl;
    }

    public RequestSpecification baseManagementRequest() {
        final RequestSpecification request = RestAssured.given().baseUri(this.controlPlaneManagement.get().toString());
        return this.enrichManagementRequest.apply(request);
    }

    protected UnaryOperator<RequestSpecification> enrichManagementRequest = r ->
            r.headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE,
                    HttpHeaderNames.AUTHORIZATION, "Bearer " + token);

    private static class SeedVaultKeys implements ServiceExtension {

        @Inject
        private Vault vault;

        @Override
        public void initialize(ServiceExtensionContext context) {
            try {
                var kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                var keyPair = kpg.generateKeyPair();

                var privateKey = encode(keyPair.getPrivate());
                var publicKey = encode(keyPair.getPublic());

                vault.storeSecret("private-key-alias", privateKey);
                vault.storeSecret("public-key-alias", publicKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static @NotNull String encode(AsymmetricKey key) {
            var type = switch (key) {
                case PublicKey _ -> "PUBLIC";
                case PrivateKey _ -> "PRIVATE";
                default -> throw new EdcException("not possible");
            };

            return """
            -----BEGIN %s KEY-----
            %s
            -----END %s KEY-----
            """.formatted(type, Base64.getMimeEncoder().encodeToString(key.getEncoded()), type);
        }
    }

    private static String getCredentialsPath(String participant) {
        System.err.println("Resource path being searched");
        URL resourceUrl = OaebudtParticipant.class.getClassLoader()
                .getResource("assets/did/v-credentials/"+participant+"/");
        if (resourceUrl == null) {
            throw new IllegalStateException("Credentials path not found in resources");
        }
        System.out.println("Credentials path :" + resourceUrl.getPath().toString());
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
