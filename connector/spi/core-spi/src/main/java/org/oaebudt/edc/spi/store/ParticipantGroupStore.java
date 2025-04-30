package org.oaebudt.edc.spi.store;

import java.util.Map;
import java.util.Set;

public interface ParticipantGroupStore {

    Set<String> getParticipantsByGroupId(String groupId);

    void putParticipantsInGroup(String groupId, String... participantIds);

    Map<String, Set<String>> getAllGroups();
}
