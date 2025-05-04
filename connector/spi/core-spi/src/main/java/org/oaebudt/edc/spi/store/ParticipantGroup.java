package org.oaebudt.edc.spi.store;

import java.util.Set;

public record ParticipantGroup(String id, Set<String> participants) {
}
