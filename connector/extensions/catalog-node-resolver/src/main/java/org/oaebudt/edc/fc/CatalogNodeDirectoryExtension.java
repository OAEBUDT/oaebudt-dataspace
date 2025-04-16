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
    private Monitor monitor;
    private TargetNodeDirectory nodeDirectory;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var participantsPath = context.getConfig().getString(PARTICIPANT_LIST_FILE_PATH);
        monitor = context.getMonitor();

        participantListFile = new File(participantsPath).getAbsoluteFile();
        if (!participantListFile.exists()) {
            monitor.warning("Path '%s' does not exist. It must be a resolvable path with read access. Will not add any VCs.".formatted(participantsPath));
        }
    }

    @Provider
    public TargetNodeDirectory createLazyTargetNodeDirectory() {
        if (nodeDirectory == null) {
            nodeDirectory = new CatalogNodeDirectory(typeManager.getMapper(), participantListFile, didResolverRegistry, monitor);
        }
        return nodeDirectory;
    }

    @Provider
    public TargetNodeFilter skipSelfNodeFilter(ServiceExtensionContext context) {
        return targetNode -> {
            var predicateTest = !targetNode.id().equals(context.getParticipantId());
            if (!predicateTest) {
                monitor.debug("Node filter: skipping node '%s' for participant '%s'".formatted(targetNode.id(), context.getParticipantId()));
            }
            return predicateTest;
        };
    }
}
