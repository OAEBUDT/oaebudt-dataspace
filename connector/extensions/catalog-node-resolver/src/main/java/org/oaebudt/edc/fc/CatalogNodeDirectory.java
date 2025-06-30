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

package org.oaebudt.edc.fc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link TargetNodeDirectory} that is initialized with a file, that contains participant DIDs. On the first getAll() request
 * the DIDs are resolved and converted into {@link TargetNode} objects. From then on, they are held in memory and cached.
 * <p>
 * DIDs must contain a {@link org.eclipse.edc.iam.did.spi.document.Service} where the {@link Service#getType()} equals {@code ProtocolEndpoint}
 */
public class CatalogNodeDirectory implements TargetNodeDirectory {
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String PROTOCOL_ENDPOINT = "ProtocolEndpoint";

    private final ObjectMapper mapper;
    private final File participantListFile;
    private final DidResolverRegistry didResolverRegistry;
    private final Monitor monitor;

    public CatalogNodeDirectory(ObjectMapper mapper, File participantListFile, DidResolverRegistry didResolverRegistry, Monitor monitor) {

        this.mapper = mapper;
        this.participantListFile = participantListFile;
        this.didResolverRegistry = didResolverRegistry;
        this.monitor = monitor;
    }

    @Override
    public List<TargetNode> getAll() {
        try {
            var entries = mapper.readValue(participantListFile, MAP_TYPE);

            return entries.entrySet().stream()
                    .map(e -> createNode(e.getKey(), e.getValue()))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public void insert(TargetNode targetNode) {
        //noop
    }

    private TargetNode createNode(String name, String did) {
        var didResult = didResolverRegistry.resolve(did);
        if (didResult.failed()) {
            monitor.warning(didResult.getFailureDetail());
            return null;
        }
        var document = didResult.getContent();
        var service = document.getService().stream().filter(s -> s.getType().equalsIgnoreCase(PROTOCOL_ENDPOINT)).findFirst();
        return service.map(s -> new TargetNode(name, did, s.getServiceEndpoint(), List.of("dataspace-protocol-http")))
                .orElse(null);
    }
}
