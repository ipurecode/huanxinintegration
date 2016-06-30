package com.purecode.imapp.model.datamodel;

/**
 * Created by purecode on 16/6/20.
 */
public class IMInvitationGroupInfo {
    private String groupName;
    private String groupId;
    private String inviteTriggerUser;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getInviteTriggerUser() {
        return inviteTriggerUser;
    }

    public void setInviteTriggerUser(String inviter) {
        this.inviteTriggerUser = inviter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("group name : " + groupName);
        sb.append("|");
        sb.append("group id : " +groupId);
        sb.append("|");
        sb.append("trigger : " + inviteTriggerUser);
        return sb.toString();
    }
}
