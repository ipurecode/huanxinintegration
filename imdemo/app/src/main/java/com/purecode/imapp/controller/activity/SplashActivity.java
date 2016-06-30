package com.purecode.imapp.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.purecode.imapp.R;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.Model;
import com.hyphenate.chat.EMClient;

/**
 * Created by purecode on 2016/5/18.
 */
public class SplashActivity extends Activity {
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        handler =  new Handler() {
            public void handleMessage(Message msg) {
                onInit();
            }
        };


        handler.sendMessageDelayed(Message.obtain(), 3 * 1000);
    }

    private void onInit() {

        if(EMClient.getInstance().isLoggedInBefore()){
            Model.getInstance().globalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    IMUser account = Model.getInstance().getUserAccountHandler().getAccountByHxId(EMClient.getInstance().getCurrentUser());

                    Model.getInstance().onLoginSuccess(account);

                    // 保证加载本地群，加载所有的群到内存中
                    EMClient.getInstance().groupManager().loadAllGroups();

                    // 确保加载本地会话，实际上SDK查询SqliteDB加载所有会话到内存里
                    EMClient.getInstance().chatManager().loadAllConversations();

                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }
                    });
                }
            });
        }else{
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }
    }
}
