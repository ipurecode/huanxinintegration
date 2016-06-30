package com.purecode.imapp.model;

import android.content.Context;

import com.purecode.imapp.model.datamodel.InvitationInfo;

import java.util.List;

/**
 * Created by purecode on 2016/6/29.
 *
 * 联系人和群的邀请和申请的管理器
 *
 * 用来获取相关的邀请和申请信息，并且提供更改邀请信息的状态
 */
public class InvitationHandler extends HandlerBase {
    InvitationHandler(Context context) {
        super(context);
    }

    public List<InvitationInfo> getInvitationInfo(){
        return getDbManager().getInvitations();
    }

    public void removeInvitation(String user) {
        getDbManager().removeInvitation(user);
    }

    public void updateInvitation(InvitationInfo.InvitationStatus status,String hxId){
        getDbManager().updateInvitationStatus(status, hxId);
    }

    public void updateInviteNotif(boolean hasNotify){
        getDbManager().updateInvitateNoify(hasNotify);
    }

    public boolean hasInviteNotif(){
        return getDbManager().hasInviteNotif();
    }

    public void acceptGroupInvitation(InvitationInfo invitationInfo){
        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_ACCEPT_INVITE);

        getDbManager().addInvitation(invitationInfo);
    }

    public void acceptGroupApplication(InvitationInfo invitationInfo){
        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUPO_ACCEPT_APPLICATION);

        getDbManager().addInvitation(invitationInfo);
    }

    public void rejectGroupInvitation(InvitationInfo invitationInfo){
        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_REJECT_INVITE);

        getDbManager().addInvitation(invitationInfo);
    }

    public void rejectGroupApplication(InvitationInfo invitationInfo){
        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_REJECT_APPLICATION);

        getDbManager().addInvitation(invitationInfo);

    }
}
