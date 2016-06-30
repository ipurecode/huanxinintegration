package com.purecode.imapp.event;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.purecode.imapp.common.Constant;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by purecode on 16/6/21.
 */
public class GlobalEventNotifer {
    private Context context;
    private List<EMContactListener> contactListeners;
    private List<EMGroupChangeListener> groupChangeListeners;
    private List<OnSyncListener> contactSyncListeners;

    private static GlobalEventNotifer instance;

    public static GlobalEventNotifer getInstance(){
        if(instance == null){
            instance = new GlobalEventNotifer();
        }

        return instance;
    }

    public void init(Context context){
        this.context = context;

        contactListeners = new ArrayList<>();
        groupChangeListeners = new ArrayList<>();
        contactSyncListeners = new ArrayList<>();

        initListener();

    }

    public void addContactListeners(EMContactListener listener){
        if(contactListeners.contains(listener)){
            return;
        }

        contactListeners.add(listener);
    }

    public void removeContactListener(EMContactListener listener){
        contactListeners.remove(listener);
    }

    public void addGroupChangeListener(EMGroupChangeListener groupChangeListener){
        if(groupChangeListeners.contains(groupChangeListener)){
            return;
        }

        groupChangeListeners.add(groupChangeListener);
    }

    public void removeGroupChangeListener(EMGroupChangeListener groupChangeListener){
        if(groupChangeListener == null){
            return;
        }

        groupChangeListeners.remove(groupChangeListener);
    }

    public void addOnContactSyncListener(OnSyncListener listener){
        if(listener == null){
            return;
        }

        if(contactSyncListeners.contains(listener)){
            return;
        }

        contactSyncListeners.add(listener);
    }

    public void notifyContactSyncChanged(boolean success){
        for(OnSyncListener listener:contactSyncListeners){
            if(success){
                listener.onSuccess();
            }else{
                listener.onFailed();
            }
        }
    }

    private void initListener() {
        EMClient.getInstance().groupManager().addGroupChangeListener(groupChangeListener);
        EMClient.getInstance().contactManager().setContactListener(contactListener);
    }

    private EMGroupChangeListener groupChangeListener = new EMGroupChangeListener() {
        @Override
        public void onInvitationReceived(String s, String s1, String s2, String s3) {
            for (EMGroupChangeListener listener:groupChangeListeners){
                listener.onInvitationReceived(s,s1,s2,s3);
            }

            broadCastGroupInvitation();
        }

        @Override
        public void onApplicationReceived(String s, String s1, String s2, String s3) {
            for (EMGroupChangeListener listener:groupChangeListeners){
                listener.onApplicationReceived(s,s1,s2,s3);
            }

            broadCastGroupInvitation();
        }

        @Override
        public void onApplicationAccept(String s, String s1, String s2) {
            for(EMGroupChangeListener listener:groupChangeListeners){
                listener.onApplicationAccept(s,s1,s2);
            }

        }

        @Override
        public void onApplicationDeclined(String s, String s1, String s2, String s3) {
            for (EMGroupChangeListener listener:groupChangeListeners){
                listener.onApplicationDeclined(s,s1,s2,s3);
            }

            broadCastGroupInvitation();
        }

        @Override
        public void onInvitationAccpted(String s, String s1, String s2) {
            for(EMGroupChangeListener listener:groupChangeListeners){
                listener.onInvitationAccpted(s, s1, s2);
            }
        }

        @Override
        public void onInvitationDeclined(String s, String s1, String s2) {
            for (EMGroupChangeListener listener:groupChangeListeners){
                listener.onInvitationDeclined(s, s1, s2);
            }

            broadCastGroupInvitation();
        }

        @Override
        public void onUserRemoved(String s, String s1) {
            broadCastGroupChanged();
        }

        @Override
        public void onGroupDestroy(String s, String s1) {
            broadCastGroupChanged();
        }

        @Override
        public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            broadCastGroupInvitation();
        }
    };

    private EMContactListener contactListener = new EMContactListener() {
        @Override
        public void onContactAdded(String s) {
            for(EMContactListener listener:contactListeners){
                listener.onContactAdded(s);
            }

            broadCastContactChanged();
        }

        @Override
        public void onContactDeleted(String s) {
            for (EMContactListener listener:contactListeners){
                listener.onContactDeleted(s);
            }

            broadCastContactChanged();
        }

        @Override
        public void onContactInvited(String s, String s1) {
            for(EMContactListener listener:contactListeners){
                listener.onContactInvited(s, s1);
            }

            broadCastContactInvitation();
        }

        @Override
        public void onContactAgreed(String s) {
            for(EMContactListener listener:contactListeners){
                listener.onContactAgreed(s);
            }
        }

        @Override
        public void onContactRefused(String s) {
            for(EMContactListener listener:contactListeners){
                listener.onContactRefused(s);
            }

            broadCastContactInvitation();
        }
    };

    private void broadCastGroupChanged(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

        lbm.sendBroadcast(new Intent(Constant.GROUP_CHANGED));
    }

    private void broadCastGroupInvitation(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

        lbm.sendBroadcast(new Intent(Constant.GROUP_INVITATION_MESSAGE_CHANGED));
    }

    private void broadCastContactInvitation(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

        lbm.sendBroadcast(new Intent(Constant.CONTACT_INVITATION_CHANGED));
    }

    private void broadCastContactChanged(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

        lbm.sendBroadcast(new Intent(Constant.CONTACT_CHANGED));
    }
}
