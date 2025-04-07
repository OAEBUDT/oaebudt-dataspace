package org.oaebudt.edc.dcp.policy;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.spi.result.Result;

import java.util.List;

public class AbstractCredentialEvaluationFunction {
    protected static final String MVD_NAMESPACE = "https://w3id.org/mvd/credentials/";

    private final CredentialExtractor credentialExtractor;

    public AbstractCredentialEvaluationFunction(CredentialExtractor credentialExtractor) {
        this.credentialExtractor = credentialExtractor;
    }

    protected Result<List<VerifiableCredential>> getCredentialList(ParticipantAgent agent) {
        return credentialExtractor.extractCredentials(agent);
    }
}
