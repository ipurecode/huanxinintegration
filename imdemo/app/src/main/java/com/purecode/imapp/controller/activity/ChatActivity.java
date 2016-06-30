package com.purecode.imapp.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.purecode.imapp.R;
import com.purecode.imapp.common.Constant;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;

/**
 * Created by purecode on 2016/5/20.
 */
public class ChatActivity extends FragmentActivity{
    private EaseChatFragment mChatFragment;
    private String hxId;
    private int chatType;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        mChatFragment = new EaseChatFragment();

        hxId = getIntent().getExtras().getString(EaseConstant.EXTRA_USER_ID);

        chatType = getIntent().getExtras().getInt(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);

        mChatFragment.setArguments(getIntent().getExtras());

        setContentView(R.layout.activity_chat);

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fragment_chat, mChatFragment);

        ft.commit();

        mChatFragment.setChatFragmentListener(new EaseChatFragment.EaseChatFragmentListener() {
            @Override
            public void onSetMessageAttributes(EMMessage message) {

            }

            @Override
            public void onEnterToChatDetails() {
                Intent intent = new Intent(ChatActivity.this,GroupDetailActivity.class);
                intent.putExtra(Constant.GROUP_ID,hxId);

                startActivity(intent);
            }

            @Override
            public void onAvatarClick(String username) {

            }

            @Override
            public boolean onMessageBubbleClick(EMMessage message) {
                return false;
            }

            @Override
            public void onMessageBubbleLongClick(EMMessage message) {

            }

            @Override
            public boolean onExtendMenuItemClick(int itemId, View view) {
                return false;
            }

            @Override
            public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
                return null;
            }
        });


        if(chatType == EaseConstant.CHATTYPE_GROUP){
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(hxId.equals(intent.getStringExtra(Constant.GROUP_ID))){
                        finish();
                    }
                }
            };

            IntentFilter filter = new IntentFilter(Constant.EXIT_GROUP_ACTION);

            localBroadcastManager.registerReceiver(receiver,filter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(receiver != null){
            localBroadcastManager.unregisterReceiver(receiver);
        }
    }
}
