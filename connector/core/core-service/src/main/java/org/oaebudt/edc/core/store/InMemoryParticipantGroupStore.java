package org.oaebudt.edc.core.store;

import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InMemoryParticipantGroupStore implements ParticipantGroupStore {

    private final Map<String, ParticipantGroup> participantGroups = new HashMap<>();

    @Override
    public ParticipantGroup findById(String groupId) {
        return participantGroups.get(groupId);
    }

    @Override
    public void save(String groupId, String... participantIds) {
        if(participantGroups.containsKey(groupId)) {
            participantGroups.get(groupId).participants().addAll(Arrays.asList(participantIds));
            return;
        }
        participantGroups.put(groupId, new ParticipantGroup(groupId, new HashSet<>(Arrays.asList(participantIds))));
    }

    @Override
    public Set<ParticipantGroup> getAllGroups() {
        return new HashSet<>(participantGroups.values());
    }
}
