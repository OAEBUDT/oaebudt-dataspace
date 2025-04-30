package org.oaebudt.edc.core.store;

import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InMemoryParticipantGroupStore implements ParticipantGroupStore {

    private Map<String, Set<String>> participantGroups = new HashMap<>();

    @Override
    public Set<String> getParticipantsByGroupId(String groupId) {
        return participantGroups.get(groupId);
    }

    @Override
    public void putParticipantsInGroup(String groupId, String... participantIds) {
        if(participantGroups.containsKey(groupId)) {
            participantGroups.get(groupId).addAll(Arrays.asList(participantIds));
            return;
        }
        participantGroups.put(groupId, new HashSet<>(Arrays.asList(participantIds)));
    }

    @Override
    public Map<String, Set<String>> getAllGroups() {
        return participantGroups;
    }
}
