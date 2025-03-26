package org.oaebudt.edc.dcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.keys.keyparsers.PemParser;
import org.eclipse.edc.security.token.jwt.CryptoConverter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

/**
 * Use this test to read a verifiable credential from the file system, and sign it with a given private key. You will need:
 * <ul>
 *     <li>A JSON file containing the VC</li>
 *     <li>A public/private key pair in either JWK or PEM format</li>
 * </ul>
 */
@SuppressWarnings("NewClassNamingConvention")
public class JwtSigner {

    public static final String ISSUER_PRIVATE_KEY_FILE_PATH = System.getProperty("user.dir") + "/../../deployment/ide/assets/did/cryptographic-keys/did-issuer/private.pem";
    public static final String ISSUER_PUBLIC_KEY_FILE_PATH = System.getProperty("user.dir") + "/../../deployment/ide/assets/did/cryptographic-keys/did-issuer/public.pem";
    public static final File ISSUER_DID_DOCUMENT_LOCAL = new File(System.getProperty("user.dir") + "/../../deployment/ide/dependencies/did-issuer/did.json");
    public static final String DATASPACE_ISSUER_DID_LOCAL = "did:web:localhost%3A1999";
    private final ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @ArgumentsSource(InputOutputProvider.class)
    void generateJwt(String rawCredentialFilePath, File vcResource, String did, String issuerDid, File issuerDidDocument) throws JOSEException, IOException {

        var header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                .keyID(issuerDid + "#key-1")
                .type(JOSEObjectType.JWT)
                .build();


        var credential = mapper.readValue(new File(rawCredentialFilePath), Map.class);

        var claims = new JWTClaimsSet.Builder()
                .audience(did)
                .subject(did)
                .issuer(issuerDid)
                .claim("vc", credential)
                .issueTime(Date.from(Instant.now()))
                .build();

        // this must be the path to the Credential issuer's private key
        var privateKey = (PrivateKey) new PemParser(mock()).parse(readFile(ISSUER_PRIVATE_KEY_FILE_PATH)).orElseThrow(f -> new RuntimeException(f.getFailureDetail()));
        var publicKey = (PublicKey) new PemParser(mock()).parse(readFile(ISSUER_PUBLIC_KEY_FILE_PATH)).orElseThrow(f -> new RuntimeException(f.getFailureDetail()));

        // sign raw credentials with new issuer public key
        var jwt = new SignedJWT(header, claims);
        jwt.sign(CryptoConverter.createSignerFor(privateKey));

        // replace the "rawVc" field in the VC resources file, so that it gets seeded to the database
        var content = Files.readString(vcResource.toPath());
        var updatedContent = content.replaceFirst("\"rawVc\":.*,", "\"rawVc\": \"%s\",".formatted(jwt.serialize()));
        Files.write(vcResource.toPath(), updatedContent.getBytes());

        // update issuer DID document with new public key
        var issuerJwk = CryptoConverter.createJwk(new KeyPair(publicKey, null));
        var didDoc = mapper.readValue(issuerDidDocument, DidDocument.class);

        var issuerPk = didDoc.getVerificationMethod().get(0).getPublicKeyJwk();
        issuerPk.clear();
        issuerPk.putAll(issuerJwk.toPublicJWK().toJSONObject());
        Files.write(issuerDidDocument.toPath(), mapper.writeValueAsBytes(didDoc));
    }

    private String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class InputOutputProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(

                    // participant-a credentials
                    Arguments.of(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-a/unsigned/membership_vc.json",
                            new File(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-a/membership-credential.json"),
                            "did:web:participant-a-identityhub%3A6100:jayshree", DATASPACE_ISSUER_DID_LOCAL, ISSUER_DID_DOCUMENT_LOCAL),

                    Arguments.of(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-a/unsigned/dataprocessor_vc.json",
                            new File(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-a/dataprocessor-credential.json"),
                            "did:web:participant-a-identityhub%3A6100:jayshree", DATASPACE_ISSUER_DID_LOCAL, ISSUER_DID_DOCUMENT_LOCAL),

                    // participant-b credentials
//                    Arguments.of(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-b/unsigned/membership_vc.json",
//                            new File(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-b/membership-credential.json"),
//                            "did:web:consumer-identityhub%3A7083:alice", DATASPACE_ISSUER_DID_LOCAL, ISSUER_DID_DOCUMENT_LOCAL),
//
//                    Arguments.of(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-b/unsigned/dataprocessor_vc.json",
//                            new File(System.getProperty("user.dir") + "/../../deployment/ide/assets/did/v-credentials/participant-b/dataprocessor-credential.json"),
//                            "did:web:consumer-identityhub%3A7083:alice", DATASPACE_ISSUER_DID_LOCAL, ISSUER_DID_DOCUMENT_LOCAL)

            );
        }
    }
}
