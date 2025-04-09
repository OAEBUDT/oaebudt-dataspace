package org.oaebudt.edc.dcp.policy;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.spi.result.Result;

import java.util.List;

public class CredentialExtractor {
    private static final String VC_CLAIM = "vc";

    public Result<List<VerifiableCredential>> extractCredentials(ParticipantAgent agent) {
        var vcListClaim = agent.getClaims().get(VC_CLAIM);

        if (vcListClaim == null) {
            return Result.failure("ParticipantAgent did not contain a '%s' claim.".formatted(VC_CLAIM));
        }
        if (!(vcListClaim instanceof List)) {
            return Result.failure("ParticipantAgent contains a '%s' claim, but the type is incorrect. Expected %s, received %s."
                    .formatted(VC_CLAIM, List.class.getName(), vcListClaim.getClass().getName()));
        }

        var vcList = (List<VerifiableCredential>) vcListClaim;
        if (vcList.isEmpty()) {
            return Result.failure("ParticipantAgent contains a '%s' claim but it did not contain any VerifiableCredentials."
                    .formatted(VC_CLAIM));
        }

        return Result.success(vcList);
    }
}
