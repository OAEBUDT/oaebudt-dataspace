package oaebudt.dataspace.connector.utils;

import static java.util.Map.entry;
import static org.eclipse.edc.util.io.Ports.getFreePort;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.eclipse.edc.connector.controlplane.test.system.utils.Participant;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;

public class OaebudtParticipant extends Participant {

    public static final String API_KEY_HEADER_KEY = "X-Api-Key";
    public static final String API_KEY_HEADER_VALUE = "password";

    private OaebudtParticipant() {

    }

    public static class Builder extends Participant.Builder<OaebudtParticipant, Builder> {

        public static Builder newInstance() {
            return new Builder(new OaebudtParticipant());
        }

        protected Builder(final OaebudtParticipant participant) {
            super(participant);
        }
    }

    public Config getConfiguration(final int vaultPort, final int postgresPort) {
        final var map = Map.ofEntries(
                entry("edc.participant.id", id),
                entry("web.http.path", "/api"),
                entry("web.http.port", getFreePort() + ""),
                entry("web.http.control.path", "/control"),
                entry("web.http.control.port", getFreePort() + ""),
                entry("web.http.management.path", controlPlaneManagement.get().getPath()),
                entry("web.http.management.port", controlPlaneManagement.get().getPort() + ""),
                entry("web.http.protocol.path", controlPlaneProtocol.get().getPath()),
                entry("web.http.protocol.port", controlPlaneProtocol.get().getPort() + ""),
                entry("web.http.version.path", "/version"),
                entry("web.http.version.port", getFreePort() + ""),
                entry("web.http.public.path", "/public"),
                entry("web.http.public.port", getFreePort() + ""),
                entry("edc.transfer.proxy.token.verifier.publickey.alias", "public-key-alias"),
                entry("edc.transfer.proxy.token.signer.privatekey.alias", "private-key-alias"),
                entry("edc.iam.issuer.id", "http://mock-issuer"),
                entry("edc.api.auth.key", "password"),
                entry("edc.iam.sts.publickey.id", "dummy-key"),
                entry("edc.iam.sts.publickey.alias", "dummy-key"),
                entry("edc.iam.sts.privatekey.alias", "dummy-key"),
                entry("web.http.catalog.port", getFreePort() + ""),
                entry("web.http.catalog.path", "/catalog"),
                entry("edc.runtime.id", id),
                entry("edc.dpf.selector.url", "http://localhost:" + getFreePort() + "/control/v1/dataplanes"),
                entry("edc.vault.hashicorp.url", "http://localhost:" + vaultPort),
                entry("edc.vault.hashicorp.token", "root"),
                entry("edc.vault.hashicorp.health.check.enabled", "false"),
                entry("edc.datasource.default.url", "jdbc:postgresql://localhost:" + postgresPort + "/db"),
                entry("edc.datasource.default.user", "password"),
                entry("edc.datasource.default.password", "password"),
                entry("edc.sql.schema.autocreate", "true")
        );

        return ConfigFactory.fromMap(map);
    }

    @Override
    public RequestSpecification baseManagementRequest() {
        final RequestSpecification request = RestAssured.given().baseUri(this.controlPlaneManagement.get().toString());
        return this.enrichManagementRequest.apply(request);
    }

    protected UnaryOperator<RequestSpecification> enrichManagementRequest = (r) -> r.headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE);
}
