package com.purecode.imapp.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.purecode.imapp.event.GlobalEventNotifer;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.db.PreferenceUtils;
import com.purecode.imapp.controller.activity.MainActivity;
import com.purecode.imapp.model.db.UserAccountDB;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.exceptions.HyphenateException;
import com.purecode.imapp.model.db.DBManager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by purecode on 2016/5/19.
 *
 * Model - 代表等着整个APP的数据存取模型，所有的其他的类Controller和View实体类都必须且只能通过Model类获取数据模型
 *
 */
public class Model {
    private final static String TAG = "IM Model";
    private boolean isInited = false;
    private Context mAppContext;
    private static Model me = new Model();
    private IMUser currentAccount;
    private DBManager mDBManager;
    private UserAccountDB userAccountDB;
    // handlers
    private InvitationHandler invitationHandler;
    private UserAccountHandler userAccountHandler;
    private ContactHandler contactHandler;

    private PreferenceUtils mPreference;
    private boolean isGroupSynced = false;
    private EventHandler eventHandler;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    static public Model getInstance(){
        return me;
    }

    public boolean init(Context appContext){
        if(isInited){
            return false;
        }

        mAppContext = appContext;

        EMOptions options = new EMOptions();
        options.setAutoAcceptGroupInvitation(false);
        options.setAcceptInvitationAlways(false);

        if(!EaseUI.getInstance().init(appContext, options)){
            return false;
        }

        // 1. 首先必需先初始化GlobalEventNotifer
        GlobalEventNotifer.getInstance().init(appContext);

        // 2. 创建Model Eevent listener
        userAccountDB = new UserAccountDB(appContext);
        mPreference = new PreferenceUtils(mAppContext);

        eventHandler = new EventHandler(appContext);
        invitationHandler = new InvitationHandler(appContext);
        userAccountHandler = new UserAccountHandler(appContext,userAccountDB);
        contactHandler = new ContactHandler(appContext,mPreference);

        isGroupSynced = mPreference.isContactSynced();

        isInited = true;

        initListener();
        initProvider();

        if(EMClient.getInstance().isLoggedInBefore()){
            preLogin(userAccountHandler.getAccountByHxId(EMClient.getInstance().getCurrentUser()));
        }

        return isInited;
    }

    public void logout(final EMCallBack callBack){
        EMClient.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                contactHandler.reset();

                mPreference.setGroupSynced(false);
                isGroupSynced = false;
                callBack.onSuccess();
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i, s);
            }

            @Override
            public void onProgress(int i, String s) {
                callBack.onProgress(i, s);
            }
        });
    }

    public void asyncFetchGroups(){
        globalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                    mPreference.setGroupSynced(true);
                    isGroupSynced = true;
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }

                // 取完群后要和app服务器做同步
                // 同步相关的群信息，例如群头像,和app自己定义的群的属性，然后将他们存到本地数据库

                //
                fetchGroupsFromAppServer(EMClient.getInstance().groupManager().getAllGroups());
            }
        });
    }



    public boolean isGroupSynced(){
        return isGroupSynced;
    }

    public void preLogin(IMUser account){
        if(account == null){
            return;
        }

        Log.d(TAG,"logined user name : " + account.getAppUser());

        if(currentAccount != null){
            if(currentAccount.getAppUser() == account.getAppUser()){
                return;
            }

            mDBManager.close();

        }

        currentAccount = new IMUser(account);

        mDBManager = new DBManager(mAppContext,currentAccount.getHxId());

        eventHandler.setDbManager(mDBManager);
        invitationHandler.setDbManager(mDBManager);
        userAccountHandler.setDbManager(mDBManager);
        contactHandler.setDbManager(mDBManager);
    }

    public void onLoginSuccess(IMUser user){

    }

    public InvitationHandler getInvitationHandler(){
        return invitationHandler;
    }

    public UserAccountHandler getUserAccountHandler(){
        return userAccountHandler;
    }

    public ContactHandler getContactHandler(){
        return contactHandler;
    }

    public ExecutorService globalThreadPool(){
        return executorService;
    }



    public void registerKickoffTask(Runnable kickoff){
        eventHandler.registerKickoffIntent(kickoff);
    }

    //==============================================================
    // please put private api here
    //==============================================================
    private void initProvider(){
        EaseUI.getInstance().setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {
            @Override
            public EaseUser getUser(String username) {
                IMUser user = contactHandler.getUserByHx(username);

                if (user != null) {
                    EaseUser easeUser = new EaseUser(username);

                    easeUser.setNick(user.getNick());

                    easeUser.setAvatar(user.getAvartar());

                    return easeUser;
                }

                return null;
            }
        });

        EaseUI.getInstance().getNotifier().setNotificationInfoProvider(new EaseNotifier.EaseNotificationInfoProvider() {
            @Override
            public String getDisplayedText(EMMessage message) {
                String hxId = message.getFrom();

                IMUser user = contactHandler.getUserByHx(hxId);

                if (user != null) {
                    return user.getNick() + "发来一条消息";
                }
                return null;
            }

            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                return null;
            }

            @Override
            public String getTitle(EMMessage message) {
                return null;
            }

            @Override
            public int getSmallIcon(EMMessage message) {
                return 0;
            }

            @Override
            public Intent getLaunchIntent(EMMessage message) {
                return new Intent(mAppContext, MainActivity.class);
            }
        });
    }

    private void initListener() {
    }

    private void fetchGroupsFromAppServer(List<EMGroup> allGroups) {
        //同步服务器群信息,到本地
    }
}
