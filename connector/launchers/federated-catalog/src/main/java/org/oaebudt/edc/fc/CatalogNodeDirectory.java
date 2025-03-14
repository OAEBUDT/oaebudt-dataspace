package org.oaebudt.edc.fc;

import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;

import java.util.ArrayList;
import java.util.List;

public class CatalogNodeDirectory implements TargetNodeDirectory {

    @Override
    public List<TargetNode> getAll() {
        List<String> protocolList = new ArrayList<>();
        protocolList.add("dataspace-protocol-http");

        TargetNode participantNode = new TargetNode("https://w3id.org/edc/v0.0.1/ns/",
                "participant-a",
                "http://localhost:8192/api/dsp", protocolList); //targetUrl hardcoded for the mean time. Once we setup a did registry, we get the target node based on their dsp endpoints

        return List.of(participantNode);
    }

    @Override
    public void insert(TargetNode targetNode) {

    }
}