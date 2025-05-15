package org.oaebudt.edc.spi.store;

import java.util.Set;

public record ParticipantGroup(String id, Set<String> participants) {

    public boolean contains(String participant) {
        return participants().contains(participant);
    }
}
