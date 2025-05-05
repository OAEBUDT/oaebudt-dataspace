package org.oaebudt.edc.spi.store;

import java.util.Set;

public interface ParticipantGroupStore {

    ParticipantGroup findById(String groupId);

    void save(String groupId, String... participantIds);

    Set<ParticipantGroup> getAllGroups();
}
