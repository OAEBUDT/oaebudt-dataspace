package org.oaebudt.edc.web.dto;

import java.util.Set;

public record AddParticipantToGroupRequest(String groupName, Set<String> participants) {
}
