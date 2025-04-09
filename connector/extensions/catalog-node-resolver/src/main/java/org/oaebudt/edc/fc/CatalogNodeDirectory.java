package org.oaebudt.edc.fc;

import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.spi.constants.CoreConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogNodeDirectory implements TargetNodeDirectory {

    private final List<String> participantDspEndpoints;

    public CatalogNodeDirectory(List<String> participantDspEndpoints) {
        this.participantDspEndpoints = participantDspEndpoints;
    }

    @Override
    public List<TargetNode> getAll() {
        List<String> protocolList = new ArrayList<>();
        protocolList.add("dataspace-protocol-http");

        List<TargetNode> nodes = participantDspEndpoints.stream()
                .map(dspEndpoint -> new TargetNode(CoreConstants.EDC_NAMESPACE,
                        "did:web:localhost%3A6100",
                        dspEndpoint, protocolList))
                .collect(Collectors.toList());

        // Add another node
        nodes.add(new TargetNode(
                CoreConstants.EDC_NAMESPACE,
                "did:web:localhost%3A6200",  // Different DID for the new node
                "http://localhost:6200/api/v1/dsp",  // New endpoint
                protocolList));

        return nodes;
    }

    @Override
    public void insert(TargetNode node) {

    }
}
