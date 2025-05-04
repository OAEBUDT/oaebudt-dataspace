package org.oaebudt.edc.web.service;

import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.policydefinition.PolicyDefinitionService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oaebudt.edc.web.dto.AddParticipantToGroupRequest;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantGroupServiceTest {

    private ParticipantGroupStore groupStore;
    private PolicyDefinitionService policyService;
    private Monitor monitor;
    private ParticipantGroupService service;

    @BeforeEach
    void setUp() {
        groupStore = mock(ParticipantGroupStore.class);
        policyService = mock(PolicyDefinitionService.class);
        monitor = mock(Monitor.class);
        service = new ParticipantGroupService(groupStore, policyService, monitor);
    }

    @Test
    void addParticipantsToGroup_savesGroupAndCreatesPolicy() {
        AddParticipantToGroupRequest request = new AddParticipantToGroupRequest("TrustedGroupA",
                new HashSet<>(List.of("participant1", "participant2")));

        when(policyService.create(any())).thenReturn(ServiceResult.success());

        service.addParticipantsToGroup(request);

        verify(groupStore).save(eq("trustedgroupa"), anyString(), anyString());
        verify(policyService).create(any(PolicyDefinition.class));
        verify(monitor).info(contains("created successfully"));
    }

    @Test
    void addParticipantsToGroup_whenGroupNameIsNull_shouldThrowException() {
        AddParticipantToGroupRequest request = new AddParticipantToGroupRequest(null,
                new HashSet<>(List.of("p1")));

        assertThrows(IllegalArgumentException.class, () -> service.addParticipantsToGroup(request));
        verifyNoInteractions(groupStore, policyService);
    }

    @Test
    void createTrustedGroupPolicyDefinition_whenConflict_shouldLogInfo() {
        AddParticipantToGroupRequest request = new AddParticipantToGroupRequest("GroupX",
                new HashSet<>(List.of("p1")));

        when(policyService.create(any())).thenReturn(ServiceResult.conflict("Policy already exists"));

        service.addParticipantsToGroup(request);

        verify(monitor).info(contains("already exists"));
    }

    @Test
    void createTrustedGroupPolicyDefinition_whenOtherFailure_shouldLogWarning() {
        AddParticipantToGroupRequest request = new AddParticipantToGroupRequest("GroupY",
                new HashSet<>(List.of("p1")));
        when(policyService.create(any())).thenReturn(ServiceResult.unexpected("Some internal error"));

        service.addParticipantsToGroup(request);

        verify(monitor).warning(contains("Unable to create policy definition"));
    }


    @Test
    void getAllGroups_shouldReturnStoredGroups() {
        Set<ParticipantGroup> mockGroups = Set.of(
                new ParticipantGroup("group1", Set.of("a", "b")),
                new ParticipantGroup("group2", Set.of("c"))
        );

        when(groupStore.getAllGroups()).thenReturn(mockGroups);

        Set<ParticipantGroup> result = service.getAllGroups();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.id().equals("group1")));
    }
}
