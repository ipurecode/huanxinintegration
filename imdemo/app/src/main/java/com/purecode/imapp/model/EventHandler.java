package com.purecode.imapp.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.purecode.imapp.event.GlobalEventNotifer;
import com.purecode.imapp.model.datamodel.IMInvitationGroupInfo;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.datamodel.InvitationInfo;
import com.purecode.imapp.model.db.DBManager;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import java.util.UUID;

/**
 * Created by purecode on 16/6/21.
 *
 * 事件监听器
 * 用来监听环信的好友邀请，和群的一些邀请和申请事件
 *
 * 如果app不依赖于环信的好友关系，可以去掉联系人监听部分，替换成app自己的好友监听事件
 */
class EventHandler extends HandlerBase{
    private final static String TAG ="EventHandler";
    private Runnable kickoffTask;
    private Handler mH = new Handler(Looper.getMainLooper());

    EventHandler(Context context){
        super(context);
        GlobalEventNotifer.getInstance().addGroupChangeListener(groupChangeListener);
        GlobalEventNotifer.getInstance().addContactListeners(contactListener);
        registerConnectionListener();
    }

    /**
     *  群组事件通知
     *
     *  //收到群邀请
     void onInvitationReceived (String groupId, String groupName, String inviter, String reason)

     //收到群申请通知
     void onApplicationReceived (String groupId, String groupName, String applicant, String reason)

     //收到群已经被批准加入
     void onApplicationAccept (String groupId, String groupName, String accepter)

     //收到群申请被拒绝
     void onApplicationDeclined (String groupId, String groupName, String decliner, String reason)

     //收到群邀请被同意
     void onInvitationAccpted (String groupId, String invitee, String reason)

     //收到群邀请被拒绝
     void onInvitationDeclined (String groupId, String invitee, String reason)

     void onUserRemoved (String groupId, String groupName)

     void onGroupDestroy (String groupId, String groupName)

     void onAutoAcceptInvitationFromGroup (String groupId, String inviter, String inviteMessage)
     */
    private EMGroupChangeListener groupChangeListener = new EMGroupChangeListener() {
        @Override
        public void onInvitationReceived(String s, String s1, String s2, String s3) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();
            EMGroup group = null;
            if(TextUtils.isEmpty(s1)){
                try {
                    group = EMClient.getInstance().groupManager().getGroupFromServer(s);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }

            groupInfo.setGroupId(s);
            if(!TextUtils.isEmpty(s1)){
                groupInfo.setGroupName(s1);
            }else{
                if(group != null){
                    groupInfo.setGroupName(group.getGroupName());
                }
            }

            groupInfo.setInviteTriggerUser(s2);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setReason(s3);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.NEW_GROUP_INVITE);
            invitationInfo.setGroupInfo(groupInfo);

            getDbManager().addInvitation(invitationInfo);
            getDbManager().updateInvitateNoify(true);
            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "收到邀请 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onApplicationReceived(String s, String s1, String s2, String s3) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();
            groupInfo.setGroupId(s);
            groupInfo.setGroupName(s1);
            groupInfo.setInviteTriggerUser(s2);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setReason(s3);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.NEW_GROUP_APPLICATION);
            invitationInfo.setGroupInfo(groupInfo);

            getDbManager().addInvitation(invitationInfo);
            getDbManager().updateInvitateNoify(true);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "收到申请 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onApplicationAccept(String s, String s1, String s2) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();
            groupInfo.setGroupId(s);
            groupInfo.setGroupName(s1);
            groupInfo.setInviteTriggerUser(s2);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setGroupInfo(groupInfo);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_APPLICATION_ACCEPTED);

            getDbManager().addInvitation(invitationInfo);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "申请被接受 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });

            String stringInviteAccept = " 接收了你的邀请";

            // 加群申请被同意
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(groupInfo.getInviteTriggerUser());
            msg.setTo(groupInfo.getGroupId());
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new EMTextMessageBody(groupInfo.getInviteTriggerUser() + " " + stringInviteAccept));
            msg.setStatus(EMMessage.Status.SUCCESS);

            // 保存同意消息
            EMClient.getInstance().chatManager().saveMessage(msg);
        }

        @Override
        public void onApplicationDeclined(String s, String s1, String s2, String s3) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();
            groupInfo.setGroupId(s);
            groupInfo.setGroupName(s1);
            groupInfo.setInviteTriggerUser(s2);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setReason(s3);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_APPLICATION_DECLINED);
            invitationInfo.setGroupInfo(groupInfo);

            getDbManager().addInvitation(invitationInfo);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "申请被拒绝 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onInvitationAccpted(String s, String s1, String s2) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();
            EMGroup group = EMClient.getInstance().groupManager().getGroup(s);

            groupInfo.setGroupId(s);
            groupInfo.setGroupName(s);
            if(group != null){
                groupInfo.setGroupName(group.getGroupName());
            }
            groupInfo.setInviteTriggerUser(s1);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setGroupInfo(groupInfo);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_INVITE_ACCEPTED);

            getDbManager().addInvitation(invitationInfo);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "邀请被接收 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onInvitationDeclined(String s, String s1, String s2) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();

            EMGroup group = EMClient.getInstance().groupManager().getGroup(s);

            groupInfo.setGroupId(s);
            groupInfo.setGroupName(s);
            if(group != null){
                groupInfo.setGroupName(group.getGroupName());
            }
            groupInfo.setInviteTriggerUser(s1);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setGroupInfo(groupInfo);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_INVITE_DECLINED);

            getDbManager().addInvitation(invitationInfo);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "邀请被拒绝 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onUserRemoved(String s, String s1) {

        }

        @Override
        public void onGroupDestroy(String s, String s1) {

        }

        @Override
        public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            final IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();
            groupInfo.setGroupId(s);
            groupInfo.setGroupName(s);
            groupInfo.setInviteTriggerUser(s1);

            InvitationInfo invitationInfo = new InvitationInfo();

            invitationInfo.setGroupInfo(groupInfo);
            invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_INVITE_ACCEPTED);
            getDbManager().addInvitation(invitationInfo);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "邀请被接受 : " + groupInfo, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private EMContactListener contactListener = new EMContactListener() {
        @Override
        public void onContactAdded(String s) {
            Log.d(TAG, "onContactAdded : " + s);

            Model.getInstance().getContactHandler().addHXUser(s);
        }

        @Override
        public void onContactDeleted(final String s) {
            Log.d(TAG,"onContactDeleted : " + s);

            final IMUser user = Model.getInstance().getContactHandler().getUserByHx(s);

            mH.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), "the user is removed : " + user != null?user.getNick():s, Toast.LENGTH_LONG).show();
                }
            });

            Model.getInstance().getContactHandler().deleteContactByHXID(s);
        }

        @Override
        public void onContactInvited(String hxId, String reason) {
            Log.d(TAG, "onContactInvited : " + hxId);

            getDbManager().updateInvitateNoify(true);

            IMUser user = new IMUser(hxId);

            // 从app服务器获取昵称
            // 我在这里就设置为个临时的
            fetchUserFromAppServer(user);
            InvitationInfo inviteInfo = new InvitationInfo();
            inviteInfo.setUser(user);
            inviteInfo.setReason("加个好友吧");
            inviteInfo.setStatus(InvitationInfo.InvitationStatus.NEW_INVITE);

            getDbManager().addInvitation(inviteInfo);
        }

        @Override
        public void onContactAgreed(String s) {
            Log.d(TAG, "onContactInvited : " + s);

            InvitationInfo inviteInfo = new InvitationInfo();
            inviteInfo.setReason("你的邀请已经被接受");
            inviteInfo.setStatus(InvitationInfo.InvitationStatus.INVITE_ACCEPT_BY_PEER);

            IMUser user = new IMUser(s);
            user.setNick(s);

            inviteInfo.setUser(user);

            getDbManager().addInvitation(inviteInfo);
        }

        @Override
        public void onContactRefused(String s) {
            Log.d(TAG,"onContactRefused : " + s);
        }
    };

    public void registerKickoffIntent(Runnable kickoff){
        kickoffTask = kickoff;
    }
    //从app服务器上去取用户信息
    private void fetchUserFromAppServer(IMUser user) {

    }

    private void registerConnectionListener(){
        EMClient.getInstance().addConnectionListener(new EMConnectionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected(int i) {
                if(i == EMError.USER_LOGIN_ANOTHER_DEVICE){
                    Model.getInstance().logout(new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            if(kickoffTask != null){
                                kickoffTask.run();
                            }
                        }

                        @Override
                        public void onError(int i, String s) {

                        }

                        @Override
                        public void onProgress(int i, String s) {

                        }
                    });
                }
            }
        });
    }
}
