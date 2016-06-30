package com.purecode.imapp.common;

/**
 * Created by purecode on 16/6/21.
 */
public class Constant {
    public static final String GROUP_ID = "group_id";
    // Action
    // exit the group: leave group or destroy group
    public static final String EXIT_GROUP_ACTION = "exit_group_action";

    // group change actions,used to notify the ui to get refreshed
    public static final String GROUP_CHANGED = "group_changed_action";

    // group invitation or application message changed
    public static final String GROUP_INVITATION_MESSAGE_CHANGED = "group_invitation_message_changed_action";

    // contact invitation message changed
    public static final String CONTACT_INVITATION_CHANGED = "contact_invitation_message_changed_action";

    // contact changed
    public static final String CONTACT_CHANGED = "contact_changed_action";
}
