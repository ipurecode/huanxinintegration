package com.purecode.imapp.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.purecode.imapp.R;
import com.purecode.imapp.common.Constant;
import com.purecode.imapp.model.Model;
import com.purecode.imapp.controller.fragment.ContactListFragment;
import com.purecode.imapp.controller.fragment.ConversationListFragment;
import com.purecode.imapp.controller.fragment.SettingsFragment;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.controller.EaseUI;

import java.util.List;

public class MainActivity extends FragmentActivity {

    Fragment mSetttingsFragment;
    ConversationListFragment mConversationListFragment;
    ContactListFragment mContactListFragment;
    int currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        RadioGroup group = (RadioGroup) findViewById(R.id.tab_group);

        mSetttingsFragment = new SettingsFragment();
        mConversationListFragment = new ConversationListFragment();
        mContactListFragment = new ContactListFragment();

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (currentId != checkedId) {
                    currentId = checkedId;

                    Fragment fragment = null;

                    switch (currentId) {
                        case R.id.conv_list_btn:
                            fragment = mConversationListFragment;
                            break;

                        case R.id.contact_list_btn:
                            fragment = mContactListFragment;
                            break;

                        case R.id.setting_btn:
                            fragment = mSetttingsFragment;
                            break;

                        default:
                            fragment = mSetttingsFragment;
                    }

                    switchFragment(fragment);
                }
            }
        });

        group.check(R.id.conv_list_btn);

        currentId = R.id.conv_list_btn;

        //switchFragment(mConversationListFragment);

        Model.getInstance().getContactHandler().loadLocalContacts();
        if(!Model.getInstance().getContactHandler().isContactSynced()){
            Model.getInstance().getContactHandler().asyncfetchUsers();
        }

        if(!Model.getInstance().isGroupSynced()){
            Model.getInstance().asyncFetchGroups();
        }
    }

    private void init(){
        initListener();

        Model.getInstance().registerKickoffTask(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "此账号在另外一个设备登录", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                });
            }
        });
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fragment_main, fragment);
        ft.show(fragment);

        ft.commit();
    }

    void initListener(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

        lbm.registerReceiver(contactChangedReceiver, new IntentFilter(Constant.CONTACT_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactChangedReceiver);

    }

    private EMMessageListener messageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            EaseUI.getInstance().getNotifier().onNewMesg(list);

            //如果fragment还没有attach到activity上，不能调用任何frament的api
            if(mConversationListFragment.getActivity() != null){
                mConversationListFragment.refresh();
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageChanged(EMMessage emMessage, Object o) {

        }
    };

    BroadcastReceiver contactChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mContactListFragment.getActivity() != null){
                mContactListFragment.setupContacts();
            }
        }
    };
}