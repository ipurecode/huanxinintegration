package com.purecode.imapp.model.datamodel;

/**
 * Created by purecode on 2016/5/25.
 */
public class InvitationInfo {
    private IMUser user;
    private IMInvitationGroupInfo groupInfo;

    private String reason;

    private InvitationStatus status;

    public InvitationInfo(){
    }

    public IMUser getUser() {
        return user;
    }

    public void setUser(IMUser user) {
        this.user = user;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    public InvitationInfo(String reason, IMUser user){
        this.user = user;
        this.reason = reason;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public void setGroupInfo(IMInvitationGroupInfo groupInfo){
        this.groupInfo = groupInfo;
    }

    public IMInvitationGroupInfo getGroupInfo(){
        return groupInfo;
    }

    public enum InvitationStatus{
        // contact invite status
        NEW_INVITE,
        INVITE_ACCEPT,
        INVITE_ACCEPT_BY_PEER,

        //收到邀请去加入群
        NEW_GROUP_INVITE,

        //收到申请群加入
        NEW_GROUP_APPLICATION,

        //群邀请已经被对方接受
        GROUP_INVITE_ACCEPTED,

        //群申请已经被批准
        GROUP_APPLICATION_ACCEPTED,

        //接受了群邀请
        GROUP_ACCEPT_INVITE,

        //批准的群加入申请
        GROUPO_ACCEPT_APPLICATION,

        //拒绝了群邀请
        GROUP_REJECT_INVITE,

        //拒绝了群申请加入
        GROUP_REJECT_APPLICATION,

        //群邀请被对方拒绝
        GROUP_INVITE_DECLINED,

        //群申请被拒绝
        GROUP_APPLICATION_DECLINED
    }
}
