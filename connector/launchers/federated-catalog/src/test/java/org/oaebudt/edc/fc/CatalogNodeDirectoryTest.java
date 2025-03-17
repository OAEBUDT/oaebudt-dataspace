package org.oaebudt.edc.fc;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class CatalogNodeDirectoryTest {

    static final String dspurl = "http://participantEndpoint";
    static CatalogNodeDirectory catalogNodeDirectory;

    @BeforeAll
    static void setup() {
        final Monitor monitor = new ConsoleMonitor(ConsoleMonitor.Level.WARNING, true);
        final List<String> participantList = Arrays.asList(dspurl);
        catalogNodeDirectory = new CatalogNodeDirectory(participantList);
    }

    @Test
    void shouldGetAllParticipantNode() {
        Assertions.assertThat(catalogNodeDirectory.getAll())
                .matches(targetNodes -> targetNodes.getFirst().targetUrl().equals(dspurl));
    }

}
