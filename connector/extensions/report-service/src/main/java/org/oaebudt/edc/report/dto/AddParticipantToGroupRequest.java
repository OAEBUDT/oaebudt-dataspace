package org.oaebudt.edc.report.dto;

import java.util.Set;

public class AddParticipantToGroupRequest {

    String groupName;
    Set<String> participants;

    public AddParticipantToGroupRequest() {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
}
