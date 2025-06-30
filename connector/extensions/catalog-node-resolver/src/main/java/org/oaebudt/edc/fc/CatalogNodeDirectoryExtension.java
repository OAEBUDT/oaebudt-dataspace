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

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.crawler.spi.TargetNodeFilter;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import java.io.File;

@Extension(value = CatalogNodeDirectoryExtension.NAME)
public class CatalogNodeDirectoryExtension implements ServiceExtension {
    public static final String NAME = "Participant Resolver Extension";

    public static final String PARTICIPANT_LIST_FILE_PATH = "oaebudt.ds.participants.list.file";

    @Inject
    private TypeManager typeManager;

    @Inject
    private DidResolverRegistry didResolverRegistry;

    private File participantListFile;
    private TargetNodeDirectory nodeDirectory;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var participantsPath = context.getConfig().getString(PARTICIPANT_LIST_FILE_PATH);

        participantListFile = new File(participantsPath).getAbsoluteFile();
        if (!participantListFile.exists()) {
            context.getMonitor().warning("Path '%s' does not exist. It must be a resolvable path with read access. Will not add any VCs.".formatted(participantsPath));
        }
    }

    @Provider
    public TargetNodeDirectory createLazyTargetNodeDirectory(ServiceExtensionContext context) {
        if (nodeDirectory == null) {
            nodeDirectory = new CatalogNodeDirectory(typeManager.getMapper(), participantListFile, didResolverRegistry, context.getMonitor());
        }
        return nodeDirectory;
    }

    @Provider
    public TargetNodeFilter skipSelfNodeFilter(ServiceExtensionContext context) {
        return targetNode -> {
            var predicateTest = !targetNode.id().equals(context.getParticipantId());
            if (!predicateTest) {
                context.getMonitor().debug("Node filter: skipping node '%s' for participant '%s'".formatted(targetNode.id(), context.getParticipantId()));
            }
            return predicateTest;
        };
    }
}
