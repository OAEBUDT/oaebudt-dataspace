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

        return participantDspEndpoints.stream()
                .map(dspEndpoint -> new TargetNode(CoreConstants.EDC_NAMESPACE,
                        "participant-a",
                        dspEndpoint, protocolList)).collect(Collectors.toList());
    }

    @Override
    public void insert(TargetNode node) {

    }
}