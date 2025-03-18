package org.oaebudt.edc.fc;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CatalogNodeDirectoryTest {

    static final String dspurl = "http://participantEndpoint";
    static CatalogNodeDirectory catalogNodeDirectory;

    @BeforeAll
    static void setup() {
        final List<String> participantList = List.of(dspurl);
        catalogNodeDirectory = new CatalogNodeDirectory(participantList);
    }

    @Test
    void shouldGetAllParticipantNode() {
        Assertions.assertThat(catalogNodeDirectory.getAll())
                .matches(targetNodes -> targetNodes.getFirst().targetUrl().equals(dspurl));
    }

}
